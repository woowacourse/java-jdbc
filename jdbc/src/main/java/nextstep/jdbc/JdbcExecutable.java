package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcExecutable {

    void execute(PreparedStatement pstmt) throws SQLException;
}
