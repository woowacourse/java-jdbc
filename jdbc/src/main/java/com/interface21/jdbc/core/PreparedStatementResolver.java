package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

public class PreparedStatementResolver {

    private static final Logger log = LoggerFactory.getLogger(PreparedStatementResolver.class);

    public static void setParameters(PreparedStatement pstmt, Object... args) {
        for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
            Object argument = args[parameterIndex];
            int position = parameterIndex + 1;
            try {
                pstmt.setObject(position, argument);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }
    }
}
