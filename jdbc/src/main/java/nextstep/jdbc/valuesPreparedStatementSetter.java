package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class valuesPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] values;

    public valuesPreparedStatementSetter(Object[] values) {
        this.values = values;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }
    }
}
