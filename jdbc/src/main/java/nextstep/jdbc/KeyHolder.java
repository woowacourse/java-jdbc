package nextstep.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KeyHolder<T> {

    private final String[] columnName;
    private final List<Object> ids;

    public KeyHolder(final String... columnName) {
        this.columnName = columnName;
        this.ids = new ArrayList<>();
    }

    public void addKey(Object id) {
        ids.add(id);
    }

    @SuppressWarnings("unchecked")
    public List<T> getIds() {
        return (List<T>) Collections.unmodifiableList(ids);
    }

    public String[] getColumnName() {
        return columnName;
    }
}
