package jhc.data.datomic;

import datomic.Entity;

import java.util.*;

/**
 * Created by p14n on 29/08/2014.
 */
public class EntityAdapter implements Map {

    private final Entity e;

    public EntityAdapter(Entity e) {
        this.e = e;
    }


    @Override
    public int size() {
        return e.keySet().size();
    }

    @Override
    public boolean isEmpty() {
        return e.keySet().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return e.get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public Object get(Object key) {
        return e.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        return e.keySet();
    }

    Object lock = new Object();

    @Override
    public Collection values() {
        ArrayList values = new ArrayList();
        for (Object key : keySet())
            values.add(get(key));
        return values;
    }

    @Override
    public Set<Entry> entrySet() {
        Set<Entry>s = new HashSet<>();
        for (final Object key : keySet()){
            s.add(new Entry() {
                @Override
                public Object getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return EntityAdapter.this.get(key);
                }

                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException();

                }
            });
        }
        return s;
    }
}
