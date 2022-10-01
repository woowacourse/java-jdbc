package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StmtSettingFunction {
    PreparedStatement execute(PreparedStatement stmt, String sql) throws SQLException;
}
