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
    }

    @Test
    @DisplayName("sql 문과 arg를 넣어 데이터베이스에 데이터의 상태를 변화 할 수 있다.")
    void update() {
        String sql = """
                INSERT INTO users (account, password, email)
                VALUES (?,?,?)
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(
                "account",
                "password",
                "email"
        );

        int rowCount = jdbcTemplate.update(sql, argumentPreparedStatementSetter);

        assertAll(
                () -> assertThat(rowCount).isEqualTo(1),
                () -> verify(connection, atLeastOnce()).commit()
        );
    }

    @Test
    @DisplayName("sql 문과 rowMapper 그리고 필요한 값을 넣어 데이터베이스에서 필요한 값을 List로 가져올 수 있다.")
    void query() throws SQLException {
        String sql = """
                SELECT id, account, password, email
                FROM users
                """;

        when(resultSet.next()).thenReturn(false);

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter();

        List<String> expected = jdbcTemplate.query(sql, rowMapper, argumentPreparedStatementSetter);

        assertThat(expected).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("sql문과 rowMapper와 필요한 값을 넣어 필요한 값을 하나 받을 수 있다.,")
    void queryForObject_Success() throws SQLException {
        String sql = "SELECT name FROM users WHERE id = ?";

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(rowMapper.mapRow(any(ResultSet.class), anyInt())).thenReturn("Polla");

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(1);

        String result = jdbcTemplate.queryForObject(sql, rowMapper, argumentPreparedStatementSetter);

        assertThat(result).isEqualTo("Polla");
        verify(resultSet, times(2)).next();
    }
}
