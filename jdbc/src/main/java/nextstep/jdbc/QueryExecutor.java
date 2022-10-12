package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryExecutor<T> {

    T execute(final PreparedStatement statement) throws SQLException;
}
