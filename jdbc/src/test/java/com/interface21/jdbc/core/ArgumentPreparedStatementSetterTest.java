package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArgumentPreparedStatementSetterTest {

    @DisplayName("PreparedStatement에 올바른 순서로 매개변수를 설정한다")
    @Test
    void setValues() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Object[] args = {1, 2, 3};

        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(args);
        preparedStatementSetter.setValues(preparedStatement);

        assertAll(
                () -> verify(preparedStatement).setObject(1, 1),
                () -> verify(preparedStatement).setObject(2, 2),
                () -> verify(preparedStatement).setObject(3, 3)
        );
    }
}
