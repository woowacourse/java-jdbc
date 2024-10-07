package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.interface21.dao.DataAccessException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(final Object... args) {
        this.args = args;
    }

    public void setValues(final PreparedStatement preparedStatement) {
        AtomicInteger index = new AtomicInteger(1);
        Arrays.stream(args)
                .forEach(arg -> setValue(preparedStatement, index.getAndIncrement(), arg));
    }

    private void setValue(final PreparedStatement preparedStatement, int index, Object arg) {
        try {
            preparedStatement.setObject(index, arg);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 인자가 올바르지 않거나, 데이터베이스에 엑세스하는 중 에러가 발생했습니다. 재시도해주세요.", e);
        }
    }
}
