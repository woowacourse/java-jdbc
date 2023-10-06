package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementGenerator {
    public PreparedStatement prepareStatement(String sql, Connection conn, Object... params) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(1 + i, params[i]);
            }
            return pstmt;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
