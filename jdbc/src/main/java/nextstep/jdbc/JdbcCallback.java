package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface JdbcCallback<T> {

    T run(final PreparedStatement preparedStatement) throws SQLException;
}
