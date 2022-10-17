package nextstep.jdbc.callback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecuteCallback {

	ResultSet execute(PreparedStatement pstmt) throws SQLException;
}
