package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ArgumentPreparedStatementSetterTest {

    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        preparedStatement = Mockito.mock(PreparedStatement.class);
    }

    @Test
    @DisplayName("setObject 성공")
    void testSetValues() throws SQLException {
        // Given
        Object[] params = new Object[]{"siso", 6, true};
        ArgumentPreparedStatementSetter setter = new ArgumentPreparedStatementSetter(params);

        // When
        setter.setValues(preparedStatement);

        // Then
        verify(preparedStatement, times(1)).setObject(1, "siso");
        verify(preparedStatement, times(1)).setObject(2, 6);
        verify(preparedStatement, times(1)).setObject(3, true);
    }
}
