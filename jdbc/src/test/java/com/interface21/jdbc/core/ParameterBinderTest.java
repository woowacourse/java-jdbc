package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import com.interface21.dao.DataAccessException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParameterBinderTest {

    private PreparedStatement preparedStatement = mock();

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("지원하는 타입의 파라미터 바인딩 성공")
    @ParameterizedTest(name = "{1} 타입")
    @MethodSource("supportedTypes")
    void bindParameter_supportedTypes(Object value, String typeName) {
        ParameterBinder parameterBinder = ParameterBinder.init();

        assertDoesNotThrow(() -> parameterBinder.bindParameter(preparedStatement, 1, value));
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

    static Stream<Arguments> supportedTypes() {
        return Stream.of(
                Arguments.of(null, "Null"),
                Arguments.of(1, "Integer"),
                Arguments.of(1L, "Long"),
                Arguments.of(true, "Boolean"),
                Arguments.of(1.0f, "Float"),
                Arguments.of(1.0, "Double"),
                Arguments.of("test", "String"),
                Arguments.of(LocalDate.now(), "LocalDate"),
                Arguments.of(LocalTime.now(), "LocalTime"),
                Arguments.of(LocalDateTime.now(), "LocalDateTime"),
                Arguments.of(new Date(System.currentTimeMillis()), "Date"),
                Arguments.of(new Time(System.currentTimeMillis()), "Time")
        );
    }
}
