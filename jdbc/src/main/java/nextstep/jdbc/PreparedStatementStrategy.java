package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementStrategy {
    PreparedStatement makePreparedStatement(Connection conn) throws SQLException;
}
