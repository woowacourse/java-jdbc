package nextstep.jdbc.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementStrategy {
    PreparedStatement createPreparedStatement(Connection c) throws SQLException;
}
