package nextstep.jdbc.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementArgumentCallback {

    PreparedStatement makePrepareStatement(Connection connection) throws SQLException;
}
