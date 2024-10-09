package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SqlParameterBinderTest {

    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("정상적인 파라미터가 PreparedStatement에 바인딩된다")
    @Test
    void bind() throws SQLException {
        Object[] params = {1, "test", 3.14};

        SqlParameterBinder.bind(preparedStatement, params);

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, "test");
        verify(preparedStatement).setObject(3, 3.14);
    }

    @DisplayName("null 파라미터가 전달될 경우 바인딩 작업을 수행하지 않는다")
    @Test
    void bindNullArgs() throws SQLException {
        Object[] params = null;

        SqlParameterBinder.bind(preparedStatement, params);

        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @DisplayName("빈 배열 파라미터가 전달될 경우 바인딩 작업을 수행하지 않는다")
    @Test
    void bindEmptyArgs() throws SQLException {
        Object[] params = new Object[0];

        SqlParameterBinder.bind(preparedStatement, params);

        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @DisplayName("바인딩 작업에 실패할 경우 발생하는 SqlException은 DataAccessException으로 변환된다")
    @Test
    void failBind() throws SQLException {
        doThrow(new SQLException("바인딩 실패")).when(preparedStatement).setObject(1, 1);

        Object[] params = {1};

        assertThatThrownBy(() -> SqlParameterBinder.bind(preparedStatement, params))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("PreparedStatement에 파라미터를 바인딩하는 데 실패했습니다. 인덱스: 1, 값: 1. 원인: 바인딩 실패");
    }
}
