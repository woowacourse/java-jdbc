package com.interface21.jdbc.core.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.interface21.jdbc.core.SqlParameterSource;
import com.interface21.jdbc.core.mock.User;

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

    @DisplayName("SqlParameterSource를 입력하면 SQL문 파라미터에 값을 바인딩하여 반환한다.")
    @Test
    void bindingParametersWithSqlParameterSource() {
        // Given
        final User user = new User(1L, "kelly", "kellyPw1234!", "kelly@email.com");

        final String baseQuery = "INSERT INTO users (account, password, email) VALUES (:account, :password, :email)";
        final Sql sql = new Sql(baseQuery);

        // When
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(user);
        final Sql bindingParametersQuery = sql.bindingParameters(sqlParameterSource);

        // Then
        final String expect = "INSERT INTO users (account, password, email) VALUES ('kelly', 'kellyPw1234!', 'kelly@email.com')";

        final String value = bindingParametersQuery.getValue();
        assertThat(value).isEqualTo(expect);
    }

    @DisplayName("null을 입력하면 예외를 발생시킨다.")
    @Test
    void bindingParametersWithNullSource() {
        // Given
        final String baseQuery = "INSERT INTO users (account, password, email) VALUES (:account, :password, :email)";
        final Sql sql = new Sql(baseQuery);

        // When & Then
        final SqlParameterSource sqlParameterSource = null;
        assertThatThrownBy(() -> sql.bindingParameters(sqlParameterSource))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parameter source는 null이 입력될 수 없습니다.");
    }

    @DisplayName("Map을 입력하면 SQL문 파라미터에 값을 바인딩하여 반환한다.")
    @Test
    void bindingParametersWithMap() {
        // Given
        final String baseQuery = "SELECT * FROM users WHERE id = :id";
        final Sql sql = new Sql(baseQuery);

        // When
        final Map<String, Object> parameters = Map.of("id", 1);
        final Sql bindingParametersQuery = sql.bindingParameters(parameters);

        // Then
        final String expect = "SELECT * FROM users WHERE id = 1";

        final String value = bindingParametersQuery.getValue();
        assertThat(value).isEqualTo(expect);
    }

    @DisplayName("null을 입력하면 예외를 발생시킨다.")
    @Test
    void bindingParametersWithNullMap() {
        // Given
        final String baseQuery = "INSERT INTO users (account, password, email) VALUES (:account, :password, :email)";
        final Sql sql = new Sql(baseQuery);

        // When & Then
        final Map<String, Object> nullMap = null;
        assertThatThrownBy(() -> sql.bindingParameters(nullMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parameter map은 null이 입력될 수 없습니다.");
    }
}
