package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementUtils {

    public static PreparedStatement getPreparedStatement(final String sql, final Object[] obj, final Connection conn) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < obj.length; i++) {
            pstmt.setObject(i + 1, obj[i]);
        }
        return pstmt;
    }

    private PreparedStatementUtils() {
    }
}
