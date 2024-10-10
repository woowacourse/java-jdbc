package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement ps) {
        IntStream.range(0, args.length)
                .forEach(parameterIndex -> setParameter(ps, parameterIndex + 1, args[parameterIndex]));
    }

    private void setParameter(PreparedStatement ps, int index, Object value) {
        try {
            ps.setObject(index, value);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Parameter 설정에 실패했습니다. value = %s", value), e);
        }
    }
}
