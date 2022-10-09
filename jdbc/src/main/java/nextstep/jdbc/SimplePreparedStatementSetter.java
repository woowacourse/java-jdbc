package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimplePreparedStatementSetter implements PreparedStatementSetter {

    private final String sql;
    private final Object[] args;

    public SimplePreparedStatementSetter(final String sql, final Object[] args) {
        this.sql = sql;
        this.args = args;
    }

    @Override
    public PreparedStatement setValues(final Connection connection) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int index = 0; index < args.length; index++) {
            pstmt.setObject(index + 1, args[index]);
        }
        return pstmt;
    }
}
