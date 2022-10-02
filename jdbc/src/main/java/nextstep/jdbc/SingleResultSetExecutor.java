package nextstep.jdbc;

import java.sql.Connection;
import java.util.List;

public interface SingleResultSetExecutor<T> {

    T execute(final Connection connection, final String sql, final Object[] columns);
}
