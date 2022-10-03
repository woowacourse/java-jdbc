package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack {
    PreparedStatement execute(PreparedStatement stmt, String sql, Object[] args) throws SQLException;
}
