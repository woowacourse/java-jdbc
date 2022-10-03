package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface JdbcAction {
    Object doAction(PreparedStatement statement) throws SQLException;
}
