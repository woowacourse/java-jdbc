package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ActionTemplate {
    Object action(PreparedStatement pst, String sql, Object[] args) throws SQLException;
}
