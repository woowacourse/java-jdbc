package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementResolver {

    public static void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        if (args == null) {
            return;
        }
        for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
            int position = parameterIndex + 1;
            pstmt.setObject(position, args[parameterIndex]);
        }
    }
}
