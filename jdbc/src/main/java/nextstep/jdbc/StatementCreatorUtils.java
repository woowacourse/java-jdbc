package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreatorUtils {
    public static void setParameterValue(PreparedStatement pstm, int i, Object arg) throws SQLException {
        pstm.setObject(i, arg);
    }
}
