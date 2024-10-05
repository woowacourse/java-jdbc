package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.interface21.jdbc.core.mock.User;

class SqlParameterSourceTest {

    @DisplayName("SQL 파라미터로 바인딩할 유효한 객체를 입력하면 SqlParameterSource 객체 인스턴스를 생성해 반환한다.")
    @Test
    void createSqlParameterSourceInstance() {
        // Given
        final User user = new User(1L, "kelly", "kellyPw1234!", "kelly@email.com");

        // When
        final SqlParameterSource instance = new SqlParameterSource(user);

        // Then
        assertThat(instance).isNotNull();
    }

    @DisplayName("생성자에 null을 입력하면 예외를 발생시킨다.")
    @Test
    void validateSourceIsNull() {
        // Given
        assertThatThrownBy(() -> new SqlParameterSource(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SQL 파라미터 소스 객체는 null이 입력될 수 없습니다.");
    }

    @DisplayName("파라미터 이름을 입력하면 해당 이름에 매핑된 값을 찾아 반환한다.")
    @MethodSource("inputAndAccept")
    @ParameterizedTest
    void getParameter(final String input, final Object expect) {
        // Given
        final User user = new User(1L, "kelly", "kellyPw1234!", "kelly@email.com");
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(user);

        // When
        final Object value = sqlParameterSource.getParameter(input);

        // Then
        assertThat(value).isEqualTo(expect);
    }

    private static Stream<Arguments> inputAndAccept() {
        return Stream.of(
                Arguments.of("id", 1L),
                Arguments.of("account", "kelly"),
                Arguments.of("password", "kellyPw1234!"),
                Arguments.of("email", "kelly@email.com")
        );
    }

    @DisplayName("파라미터 이름으로 null 혹은 빈 값이 입력되면 예외를 발생시킨다.")
    @NullAndEmptySource
    @ParameterizedTest
    void validateParameterName(final String input) {
        // Given
        final User user = new User(1L, "kelly", "kellyPw1234!", "kelly@email.com");
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(user);

        // When & Then
        assertThatThrownBy(() -> sqlParameterSource.getParameter(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파라미터 이름으로 null 혹은 공백이 입력될 수 없습니다.");
    }
}
