package nextstep.jdbc.resultset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCreator {

    PreparedStatement create(Connection connection, String sql) throws SQLException;
}
