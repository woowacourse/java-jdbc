package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateCallback implements JdbcCallback<Integer> {

    @Override
    public Integer call(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeUpdate();
    }
}
