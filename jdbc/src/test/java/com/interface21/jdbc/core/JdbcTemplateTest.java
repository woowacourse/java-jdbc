package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("sql문의 파라미터 개수와 매개변수로 전달된 파라미터 개수가 일치하지 않을 시 예외를 발생시킨다")
    @Test
    void validateParameterCount() throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        when(parameterMetaData.getParameterCount()).thenReturn(3);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "test", "test", "test", "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파라미터 값의 개수가 올바르지 않습니다");
    }
}
