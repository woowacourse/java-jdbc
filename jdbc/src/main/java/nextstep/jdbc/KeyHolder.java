package nextstep.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KeyHolder<T> {

    private final String[] columnName;
    private final List<T> ids;

    public KeyHolder(final String... columnName) {
        this.columnName = columnName;
        this.ids = new ArrayList<>();
    }

    public void addKey(T id) {
        ids.add(id);
    }

    public List<T> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public String[] getColumnName() {
        return columnName;
    }
}
