package nextstep.jdbc.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback {

    PreparedStatement makePrepareStatement(Connection connection) throws SQLException;
}
