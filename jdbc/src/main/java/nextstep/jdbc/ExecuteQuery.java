package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecuteQuery<R> {

    R executeQuery(final PreparedStatement statement) throws SQLException;
}
