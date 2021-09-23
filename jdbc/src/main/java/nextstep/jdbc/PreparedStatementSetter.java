package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {
    void setValues(PreparedStatement pstm, Object... args) throws SQLException;
}
