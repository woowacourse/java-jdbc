package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PrepareStatementResolverTest {

    @ParameterizedTest
    @DisplayName("PreparedStatement에 인자를 설정한다.")
    @CsvSource({
            "'arg1', arg2",
            "100, 200",
            "true, false",
            "10.5, 20.5",
            "'2024-10-06', '2024-12-25'"
    })
    public void setArguments(Object arg1, Object arg2) throws SQLException {
        PrepareStatementResolver prepareStatementResolver = new PrepareStatementResolver();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Object[] args = new Object[]{arg1, arg2};

        prepareStatementResolver.setArguments(preparedStatement, args);

        assertAll(
                () -> verify(preparedStatement).setObject(1, arg1),
                () -> verify(preparedStatement).setObject(2, arg2)
        );
    }

}
