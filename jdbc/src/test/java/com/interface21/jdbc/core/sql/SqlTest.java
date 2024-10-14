package com.interface21.jdbc.core.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class SqlTest {

    @DisplayName("유효한 SQL 뮨자열이 입력되면 Sql 객체 인스턴스를 생성해 반환한다.")
    @Test
    void createSqlInstance() {
        // Given
        final String input = "SELECT * FROM users";

        // When
        final Sql instance = new Sql(input);

        // Then
        assertThat(instance).isNotNull();
    }

    @DisplayName("null 혹은 빈 값을 생성자에 입력하면 예외를 발생시킨다.")
    @NullAndEmptySource
    @ParameterizedTest
    void validateSqlIsNullOrBlank(final String input) {
        // When & Then
        assertThatThrownBy(() -> new Sql(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sql문은 null 혹은 공백이 입력될 수 없습니다. - " + input);
    }
}
