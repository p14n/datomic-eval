package jhc.data.datomic;

import datomic.Entity;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by p14n on 17/09/2014.
 */
public class ConvertToVert {

    public static JsonArray convertToVert(Collection list) {
        JsonArray ja = new JsonArray();
        if (list.isEmpty()) return ja;
        for (Object o : list) {
            ja.add(convertToVert(o));
        }
        return ja;
    }

    public static JsonObject convertToVert(Entity e) {
        return convertToVert(new EntityAdapter(e));
    }

    public static JsonObject convertToVert(Map m) {
        Set<Map.Entry> set = m.entrySet();
        JsonObject jo = new JsonObject();
        for (Map.Entry e : set) {
            String key = e.getKey().toString();
            Object val = convertToVert(e.getValue());
            jo.putValue(key, val);
        }
        return jo;
    }

    private static Object convertToVert(Object value) {
        if (value instanceof Map)
            return convertToVert((Map) value);
        if (value instanceof Entity)
            return convertToVert((Entity) value);
        if (value instanceof Collection)
            return convertToVert((Collection) value);
        return value.toString();
    }

}
