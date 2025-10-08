package com.interface21.jdbc.core.preparedstatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public record PreparedStatementParameter(
        Integer index,
        Object value
) {

    public PreparedStatementParameter {
        validateIndex(index);
        validateValue(value);
    }

    private void validateIndex(Integer index) {
        if (index == null || index <= 0) {
            throw new IllegalArgumentException("Parameter index는 1 이상의 값이어야 합니다.");
        }
    }

    private void validateValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Parameter value는 null일 수 없습니다.");
        }
    }

    public void bind(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setObject(index, value);
    }
}
