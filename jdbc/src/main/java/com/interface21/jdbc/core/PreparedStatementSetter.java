package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreparedStatementSetter {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PARAMETER_START_INDEX = 1;

    public static void setValue(PreparedStatement preparedStatement, Object... args) {
        try {
            setArgsToPreparedStatement(preparedStatement, args);
        } catch (SQLException e) {
            log.info("SET_PREPARED_STATEMENT_PARAMETER_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("PreparedStatement에 파라미터를 설정하던 중 오류가 발생했습니다.");
        }
    }

    private static void setArgsToPreparedStatement(
            PreparedStatement preparedStatement,
            Object[] args
    ) throws SQLException
    {
        if (args == null) {
            return;
        }

        for (int parameterIndex = PARAMETER_START_INDEX; parameterIndex <= args.length; parameterIndex++) {
            preparedStatement.setObject(parameterIndex, args[parameterIndex - PARAMETER_START_INDEX]);
        }
    }
}
