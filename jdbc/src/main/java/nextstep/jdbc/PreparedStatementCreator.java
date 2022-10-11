package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCreator {

    static PreparedStatementCreator from(final KeyHolder<?> keyHolder, final Object... params) {
        return (conn, sql) -> {
            final PreparedStatement pstmt = conn.prepareStatement(sql, keyHolder.getColumnName());
            setParamsToPreparedStatement(pstmt, params);
            return pstmt;
        };
    }

    static PreparedStatementCreator from(final Object... params) {
        return (conn, sql) -> {
            final PreparedStatement pstmt = conn.prepareStatement(sql);
            setParamsToPreparedStatement(pstmt, params);
            return pstmt;
        };
    }

    private static void setParamsToPreparedStatement(final PreparedStatement pstmt, final Object[] params)
            throws SQLException {
        int index = 1;
        for (Object param : params) {
            pstmt.setObject(index++, param);
        }
    }

    PreparedStatement create(Connection connection, String sql) throws SQLException;
}
