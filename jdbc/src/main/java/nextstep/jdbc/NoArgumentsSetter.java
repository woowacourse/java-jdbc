package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NoArgumentsSetter implements PrepareStatementSetter {

    @Override
    public void setArguments(PreparedStatement pstm, Object... args) throws SQLException {
    }
}
