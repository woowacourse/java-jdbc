package nextstep.jdbc.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback {

    PreparedStatement makePrepareStatement(Connection connection) throws SQLException;
}
