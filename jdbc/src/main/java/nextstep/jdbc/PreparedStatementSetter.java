package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {

     void setValues(PreparedStatement pstmt) throws SQLException;
}
