package nextstep.util;

import java.util.LinkedHashMap;

public class LinkedCaseInsensitiveMap<V> extends LinkedHashMap<String, V> {

    public LinkedCaseInsensitiveMap(int columnCount) {
        super(columnCount);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            final String target = ((String) key).toUpperCase();
            return super.get(target);
        }
        return null;
    }
}
