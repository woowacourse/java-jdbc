package com.interface21.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementBinderTest {

    @Test
    @DisplayName("PreparedStatement에 인자 바인딩: 인자 순서대로 바인딩")
    void bindParameters() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementBinder preparedStatementBinder = new PreparedStatementBinder();
        preparedStatementBinder.bindParameters(preparedStatement, "value1", "value2");

        verify(preparedStatement).setObject(1, "value1");
        verify(preparedStatement).setObject(2, "value2");
    }

    @Test
    @DisplayName("PreparedStatement에 인자 바인딩: 인자가 없는 경우 바인딩되지 않음")
    void bindParameters_withNullArgument_shouldBindNullSuccessfully() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementBinder preparedStatementBinder = new PreparedStatementBinder();
        preparedStatementBinder.bindParameters(preparedStatement);

        verify(preparedStatement, never()).setObject(anyInt(), any());
    }
}
