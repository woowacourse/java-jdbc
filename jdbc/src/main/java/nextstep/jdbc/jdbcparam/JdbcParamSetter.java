package nextstep.jdbc.jdbcparam;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface JdbcParamSetter {
    void setParam(PreparedStatement statement, int index, Object param) throws SQLException;
}
