package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PrepareStatementSetter {
    void setArguments(PreparedStatement pstm, Object... args) throws SQLException;
}
