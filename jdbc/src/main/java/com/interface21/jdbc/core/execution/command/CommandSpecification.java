package com.interface21.jdbc.core.execution.command;

import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;

public record CommandSpecification(
        PreparedStatementSpecification preparedStatementSpecification
) {

    public CommandSpecification {
        validatePreparedStatementSpecification(preparedStatementSpecification);
    }

    private void validatePreparedStatementSpecification(PreparedStatementSpecification preparedStatementSpecification) {
        if (preparedStatementSpecification == null) {
            throw new IllegalArgumentException("PreparedStatementSpecification는 null일 수 없습니다.");
        }
    }
}
