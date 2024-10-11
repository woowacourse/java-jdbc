package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    public static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            int sqlParamIndex = index + 1;
            pstmt.setObject(sqlParamIndex, params[index]);
        }
    }
}
