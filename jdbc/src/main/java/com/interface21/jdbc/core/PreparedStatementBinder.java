package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class PreparedStatementBinder {

    public void bindParameters(final PreparedStatement preparedStatement, final Object... args) {
        AtomicInteger index = new AtomicInteger(1);
        Arrays.stream(args)
                .forEach(arg -> bindParameter(preparedStatement, index.getAndIncrement(), arg));
    }

    private void bindParameter(final PreparedStatement preparedStatement, int index, Object arg) {
        try {
            preparedStatement.setObject(index, arg);
        } catch (SQLException e) {
            throw new IllegalArgumentException("SQL 인자가 올바르지 않아 쿼리 실행 중 에러가 발생했습니다.");
        }
    }
}
