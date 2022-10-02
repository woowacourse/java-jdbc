package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementStrategy {
    PreparedStatement makePreparedStatement(PreparedStatement pstmt) throws SQLException;
}
