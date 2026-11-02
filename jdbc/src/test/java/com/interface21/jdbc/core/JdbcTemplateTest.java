package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("select name from users")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("단건 조회 결과가 0건이면 null을 반환한다")
    void findReturnsNullWhenNoRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        String result = jdbcTemplate.find("select name from users", rs -> rs.getString("name"));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단건 조회 결과가 2건이면 예외가 발생한다")
    void findThrowsWhenTwoRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("jiyeong");

        assertThatThrownBy(() -> jdbcTemplate.find("select name from users", rs -> rs.getString("name")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("단건 조회 결과가 2건 이상입니다.");
    }
}
