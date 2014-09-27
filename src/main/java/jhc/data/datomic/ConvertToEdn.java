package jhc.data.datomic;

import clojure.lang.*;
import datomic.db.DbId;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConvertToEdn {

    private static final Keyword idKeyword = Keyword.find("db","id");

    private static DbId createId(Object id){
        return new DbId(Keyword.find("db.part","user"), id);
    }

    private static IPersistentMap convertVertToEdnObjects(AtomicInteger ids, String parent, JsonObject m) {
        Map<Object, Object> result = new HashMap<>();
        for (String key : m.getFieldNames()) {
            Object value = m.getValue(key);
            if ("id".equals(key)) {
                result.put(idKeyword, createId(value));
            } else {
                result.put(Keyword.find(parent,key), convertVertToEdnObjects(ids, key, value));
            }

        }
        if (!result.containsKey(idKeyword)) {
            DbId id = createId(ids.decrementAndGet());
            result.put(idKeyword, id);
        }
        return PersistentArrayMap.create(result);
    }

    private static Object convertVertToEdnObjects(AtomicInteger ids, String parent, Object value) {
        if (value instanceof JsonObject)
            return convertVertToEdnObjects(ids, parent, (JsonObject) value);
        if (value instanceof Iterable)
            return convertVertToEdnObjects(ids, parent, (Iterable) value);
        return value;
    }

    private static PersistentVector convertVertToEdnObjects(AtomicInteger ids, String parent, Iterable body) {
        List result = new ArrayList();
        if (body != null)
            for (Object o : body) {
                result.add(convertVertToEdnObjects(ids, parent, o));
            }
        return PersistentVector.create(result);
    }

    private static IPersistentMap replaceMapsWithRefs(IPersistentMap m) {
        Iterator it = m.iterator();
        while ( it.hasNext() ) {
            Object mo = it.next();
            IMapEntry entry = (IMapEntry)mo;
            Object value = entry.getValue();
            if (value instanceof DbId) {
            } else if (value instanceof IPersistentMap) {
                Object id = ((IPersistentMap) value).valAt(idKeyword);
                m = m.assoc(entry.key(), id);
            } else if (value instanceof IPersistentVector) {
                IPersistentVector replaced = PersistentVector.EMPTY;
                IPersistentVector vector = (IPersistentVector) value;
                for(int i=0;i<vector.length();i++){
                    Object o =vector.nth(i);
                    if (o instanceof IPersistentMap) {
                        Object id = ((IPersistentMap) o).valAt(idKeyword);
                        replaced = replaced.cons(id);
                    }
                }
                if (replaced.length()> 0)
                    m = m.assoc(entry.key(), replaced);
            }

        }
        return m;
    }

    static IPersistentVector replaceMapsWithRefs(IPersistentVector inserts) {
        int size = inserts.length();
        IPersistentVector replaced = PersistentVector.EMPTY;
        for (int i = 0; i < size; i++) {
            Object o = inserts.nth(i);
            if (o instanceof IPersistentVector) {
                replaced = replaced.cons(replaceMapsWithRefs((IPersistentVector) o));
            } else if (o instanceof IPersistentMap) {
                replaced = replaced.cons(replaceMapsWithRefs((IPersistentMap) o));
            } else {
                replaced = replaced.cons(o);
            }
        }
        return replaced;
    }

    static IPersistentVector flatten
            (IPersistentVector inserts, IPersistentVector flat) {
        for (int i = 0; i < inserts.length(); i++) {
            flat = flatten(inserts.nth(i), flat);
        }
        return flat;
    }

    private static IPersistentVector flatten(Object m, IPersistentVector flat) {
        if(m instanceof DbId) {
            return flat;
        } else if (m instanceof Map) {
            Map jo = (Map) m;
            for (Object key : jo.entrySet()) {
                Object o = ((Map.Entry) key).getValue();
                flat = flatten(o, flat);
            }
            flat = flat.cons(m);
        } else if (m instanceof Iterable) {
            for (Object o : (Iterable) m) {
                flat = flatten(o, flat);
            }
        }

        return flat;
    }

    static IPersistentVector bigIntFix(IPersistentVector edn) {
        Object[] list = new Object[edn.length()];
        for (int i = 0; i < edn.length(); i++) {
            list[i] = bigIntFix(edn.nth(i));
        }
        return PersistentVector.create(list);
    }

    private static Object bigIntFix(Object o) {
        if (o instanceof IPersistentMap) {
            IPersistentMap m = (IPersistentMap) o;
            Iterator i = m.iterator();
            while (i.hasNext()) {
                MapEntry e = (MapEntry) i.next();
                m = m.assoc(e.key(), bigIntFix(e.val()));
            }
            return m;
        } else if (o instanceof PersistentVector) {
            PersistentVector v = (PersistentVector) o;
            Object[] vals = new Object[v.length()];
            int i = 0;
            for (Object io : v) {
                vals[i++] = bigIntFix(io);
            }
            return PersistentVector.create(vals);
        } else if (o instanceof BigInt) {
            return ((BigInt) o).toBigInteger();
        } else if (o instanceof String) {
            String s = (String) o;
            if(s.endsWith("N")){
                String numeric = s.substring(0,s.length()-1);
                for(char c: numeric.toCharArray()){
                    if(!Character.isDigit(c)) return o;
                }
                return new BigInteger(numeric);
            }
            return o;
        }
        return o;
    }

    /*
        static String encode(Object inserts) {
            StringBuffer sb = new StringBuffer();
            if (inserts instanceof JsonArray) {
                JsonArray o = (JsonArray) inserts;
                sb.append("[");
                for (Object io : o) {
                    sb.append(encode(io));
                }
                sb.append("] ");
            } else if (inserts instanceof JsonObject) {
                JsonObject o = (JsonObject) inserts;
                sb.append("{");
                for (String key : o.getFieldNames()) {
                    sb.append(key).append(" ").append(encode(o.getValue(key)));
                }
                sb.append("} ");
            } else if (inserts instanceof String) {
                String o = (String) inserts;
                boolean keyword = o.startsWith(":") || o.startsWith("#") || isBigInteger(o);
                if (!keyword) sb.append("\"");
                sb.append(o);
                if (!keyword) sb.append("\"");
                sb.append(" ");
            } else {
                sb.append(inserts.toString()).append(" ");
            }
            return sb.toString();
        }

    private static boolean isBigInteger(String o) {
        if (o.charAt(o.length() - 1) == 'N') {
            char[] chars = o.toCharArray();
            for (int i = 0; i < chars.length - 1; i++)
                if (!Character.isDigit(chars[i])) return false;
            return true;
        }
        return false;
    }
*/
    public static IPersistentVector convert(JsonArray arr,String namespace) {
        PersistentVector inserts = ConvertToEdn.convertVertToEdnObjects(new AtomicInteger(-1000000), namespace, arr);
        IPersistentVector v = ConvertToEdn.flatten(inserts, PersistentVector.EMPTY);
        return ConvertToEdn.bigIntFix(ConvertToEdn.replaceMapsWithRefs(v));
    }
    public static IPersistentVector convert(JsonObject o,String namespace) {
        return convert(new JsonArray().add(o),namespace);
    }
}