package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

public final class SqlParameterBinder {

    private SqlParameterBinder() {
    }

    private static final Logger log = LoggerFactory.getLogger(SqlParameterBinder.class);

    public static void bind(PreparedStatement preparedStatement, Object[] args) {
        if (Objects.isNull(args)) {
            return;
        }
        for (int index = 1; index <= args.length; index++) {
            bindParameter(preparedStatement, args, index);
        }
    }

    private static void bindParameter(PreparedStatement preparedStatement, Object[] args, int index) {
        try {
            preparedStatement.setObject(index, args[index - 1]);
        } catch (SQLException e) {
            log.error("파라미터 바인딩에 실패하였습니다. index: {}, arg: {}, 예외 메세지: {}", index, args[index - 1], e.getMessage(), e);
            throw new DataAccessException("PreparedStatement에 파라미터를 바인딩하는 데 실패했습니다. " +
                    "인덱스: " + index + ", 값: " + args[index - 1] + ". 원인: " + e.getMessage(), e);
        }
    }
}
