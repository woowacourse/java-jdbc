package com.interface21.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementArgumentsSetterTest {

    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("파라미터가 전달되지 않으면 아무런 값도 설정하지 않는다.")
    @Test
    void setNilArguments() throws SQLException {
        // given
        PreparedStatementArgumentsSetter argumentSetter = new PreparedStatementArgumentsSetter();

        // when
        argumentSetter.setValues(preparedStatement);

        // then
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @DisplayName("파라미터에 null이 전달되면 아무런 값도 설정하지 않는다.")
    @Test
    void setNullArguments() throws SQLException {
        // given
        PreparedStatementArgumentsSetter argumentSetter = new PreparedStatementArgumentsSetter(null);

        // when
        argumentSetter.setValues(preparedStatement);

        // then
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @DisplayName("파라미터 값을 올바르게 설정한다.")
    @Test
    void setValues() throws SQLException {
        // given
        Object[] args = {"Hello", ",", " ", "world", "!"};
        PreparedStatementArgumentsSetter argumentSetter = new PreparedStatementArgumentsSetter(args);

        // when
        argumentSetter.setValues(preparedStatement);

        // then
        for (int i = 0; i < args.length; i++) {
            verify(preparedStatement).setObject(i + 1, args[i]);
        }
    }
}
