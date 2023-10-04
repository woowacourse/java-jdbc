package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementUtils {

    public static PreparedStatement getPreparedStatement(final String sql, final Object[] obj, final Connection conn) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        setSqlParameter(obj, pstmt);
        return pstmt;
    }

    private static void setSqlParameter(final Object[] obj, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < obj.length; i++) {
            pstmt.setObject(i + 1, obj[i]);
        }
    }

    private PreparedStatementUtils() {
    }
}
