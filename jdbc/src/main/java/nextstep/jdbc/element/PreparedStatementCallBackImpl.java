package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementCallBackImpl implements PreparedStatementCallBack {
    @Override
    public PreparedStatement execute(PreparedStatement stmt, String sql, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }
        return stmt;
    }
}
