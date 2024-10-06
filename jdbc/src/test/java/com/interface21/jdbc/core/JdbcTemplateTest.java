package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    @Mock
    private Connection connection;

    @Mock
    private DataSource dataSource;

    @Mock
    private ResultSet resultSet;

    @Mock
    private RowMapper<String> rowMapper;

    @Mock
    private PreparedStatement preparedStatement;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
    }

    @Test
    @DisplayName("sql 문과 arg를 넣어 데이터베이스에 데이터의 상태를 변화 할 수 있다.")
    void update() {
        String sql = """
                INSERT INTO users (account, password, email)
                VALUES (?,?,?)
                """;

        int rowCount = jdbcTemplate.update(sql, "account", "password", "email");

        assertAll(
                () -> assertThat(rowCount).isEqualTo(1),
                () -> verify(connection, atLeastOnce()).commit()
        );
    }

    @Test
    @DisplayName("sql 문과 rowMapper 그리고 필요한 값을 넣어 데이터베이스에서 필요한 값을 List로 가져올 수 있다.")
    void query() {
        String sql = """
                SELECT id, account, password, email
                FROM users
                """;

        List<String> expected = jdbcTemplate.query(sql, rowMapper);

        assertThat(expected).isInstanceOf(List.class);
    }
}
