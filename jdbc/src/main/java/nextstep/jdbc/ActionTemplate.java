package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ActionTemplate {
    <T> T action(PreparedStatement pst, String sql, Object[] args) throws SQLException;
}
