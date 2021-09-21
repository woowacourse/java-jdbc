package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentsSetter implements PrepareStatementSetter {

    @Override
    public void setArguments(PreparedStatement pstm, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstm.setObject(i + 1, args[i]);
        }
    }
}
