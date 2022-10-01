package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface SqlSetter {
    PreparedStatement execute(PreparedStatement stmt, String sql, Object[] args) throws SQLException;
}
