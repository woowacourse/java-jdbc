package com.interface21.jdbc.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ArgumentPreparedStatementSetterTest {

    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);

    @Test
    @DisplayName("Argument가 여러개일 경우 적절히 Set 해준다.")
    void setValues() throws SQLException {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(
                "폴라",
                "위브 하이");
        argumentPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setObject(1, "폴라");
        verify(preparedStatement).setObject(2, "위브 하이");
    }
}
