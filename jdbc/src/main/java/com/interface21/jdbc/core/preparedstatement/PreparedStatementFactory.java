package com.interface21.jdbc.core.preparedstatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

public class PreparedStatementFactory {

    public static PreparedStatement initialize(
            Connection connection,
            PreparedStatementSpecification specification
    ) throws SQLException {
        validateSpecification(specification);

        PreparedStatement preparedStatement = connection.prepareStatement(specification.sql());
        for (PreparedStatementParameter parameter : specification.parameters()) {
            parameter.bind(preparedStatement);
        }

        return preparedStatement;
    }

    private static void validateSpecification(
            PreparedStatementSpecification specification
    ) throws SQLSyntaxErrorException {
        long placeholderCount = specification.sql()
                .chars()
                .filter(ch -> ch == '?')
                .count();
        long parameterCount = specification.parameters().size();

        if (placeholderCount != parameterCount) {
            throw new SQLSyntaxErrorException("PreparedStatementContext의 SQL과 파라미터의 개수가 일치하지 않습니다.");
        }

        // 추가적인 검증은 필요할 때 구현 예정
    }
}
