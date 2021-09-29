package nextstep.jdbc;

import java.sql.SQLException;

public interface JdbcCallback<T> {

    T call(final ParameterizedStatement statement) throws SQLException;
}
