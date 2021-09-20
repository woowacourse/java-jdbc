package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValue(PreparedStatement pstmt, int i, Object o) throws SQLException;
}
