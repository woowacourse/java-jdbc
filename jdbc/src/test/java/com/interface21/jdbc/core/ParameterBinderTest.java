package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParameterBinderTest {

    private PreparedStatement preparedStatement = mock();

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @Test
    @DisplayName("Null 타입 파라미터 바인딩 성공")
    void bindParameter_null() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, null));

        verify(preparedStatement).setNull(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Integer 타입 파라미터 바인딩 성공")
    void bindParameter_integer() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, 1));

        verify(preparedStatement).setInt(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Long 타입 파라미터 바인딩 성공")
    void bindParameter_long() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, 1L));

        verify(preparedStatement).setLong(anyInt(), anyLong());
    }

    @Test
    @DisplayName("Boolean 타입 파라미터 바인딩 성공")
    void bindParameter_boolean() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, true));

        verify(preparedStatement).setBoolean(anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("Float 타입 파라미터 바인딩 성공")
    void bindParameter_float() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, 1.0f));

        verify(preparedStatement).setFloat(anyInt(), anyFloat());
    }

    @Test
    @DisplayName("Double 타입 파라미터 바인딩 성공")
    void bindParameter_double() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, 1.0));

        verify(preparedStatement).setDouble(anyInt(), anyDouble());
    }

    @Test
    @DisplayName("String 타입 파라미터 바인딩 성공")
    void bindParameter_string() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, "test"));

        verify(preparedStatement).setString(anyInt(), anyString());
    }

    @Test
    @DisplayName("LocalDate 타입 파라미터 바인딩 성공")
    void bindParameter_localDate() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, LocalDate.now()));

        verify(preparedStatement).setDate(anyInt(), any());
    }

    @Test
    @DisplayName("LocalTime 타입 파라미터 바인딩 성공")
    void bindParameter_localTime() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, LocalTime.now()));

        verify(preparedStatement).setTime(anyInt(), any());
    }

    @Test
    @DisplayName("LocalDateTime 타입 파라미터 바인딩 성공")
    void bindParameter_localDateTime() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, LocalDateTime.now()));

        verify(preparedStatement).setTimestamp(anyInt(), any());
    }

    @Test
    @DisplayName("Date 타입 파라미터 바인딩 성공")
    void bindParameter_date() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(
                () -> parameterBinder.bindParameter(preparedStatement, 1, new Date(System.currentTimeMillis())));

        verify(preparedStatement).setDate(anyInt(), any());
    }

    @Test
    @DisplayName("Time 타입 파라미터 바인딩 성공")
    void bindParameter_time() throws Exception {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(
                () -> parameterBinder.bindParameter(preparedStatement, 1, new Time(System.currentTimeMillis())));

        verify(preparedStatement).setTime(anyInt(), any());
    }

    @Test
    @DisplayName("지원하지 않는 타입의 파라미터 바인딩 실패")
    void bindParameter_notSupportedTypes() {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertThatThrownBy(() -> parameterBinder.bindParameter(preparedStatement, 1, new NotSupportedType()))
                .isInstanceOf(DataAccessException.class);
    }

    static class NotSupportedType {
    }
}
