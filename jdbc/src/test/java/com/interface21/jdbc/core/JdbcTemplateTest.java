package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import support.User;

class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @DisplayName("insert 테스트")
    @Test
    void insert() throws SQLException {
        String sql = "INSERT INTO users (name) VALUES (?)";
        String name = "wiib";

        jdbcTemplate.update(sql, name);

        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("update 테스트")
    @Test
    void update() throws SQLException {
        String sql = "UPDATE users SET name = ?";
        String name = "wiib";

        jdbcTemplate.update(sql, name);

        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("delete 테스트")
    @Test
    void delete() throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        Long id = 1L;

        jdbcTemplate.update(sql, id);

        verify(preparedStatement).setObject(1, id);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("쿼리 실행 중 예외가 발생한다.")
    @Test
    void sqlException() throws SQLException {
        String sql = "INSERT INTO users (name) VALUES (?";
        String name = "wiib";

        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error"));

        assertThatThrownBy(() -> jdbcTemplate.update(sql, name))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("java.sql.SQLException: SQL error");
    }

    @DisplayName("여러 개의 데이터를 조회한다.")
    @Test
    void query() throws SQLException {
        String sql = "SELECT id FROM users";

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("wiib", "atom");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<User> actual = jdbcTemplate.query(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("name")
        ));
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual).extracting("id").containsExactly(1L, 2L),
                () -> assertThat(actual).extracting("name").containsExactly("wiib", "atom")
        );
    }

    @DisplayName("단일 데이터를 조회한다.")
    @Test
    void queryForObject1() throws SQLException {
        String sql = "SELECT id FROM users WHERE id = 1";

        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("wiib");

        User actual = jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("name")
        ), 1L);

        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual.getName()).isEqualTo("wiib")
        );
    }

    @DisplayName("단일 데이터 조회에서 2개 이상의 데이터가 나오면 예외가 발생한다.")
    @Test
    void queryForObject2() throws SQLException {
        String sql = "SELECT id FROM users";

        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("wiib", "atom");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("name")
        ), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("IncorrectResultSizeDataAccessException");
    }

    @DisplayName("단일 데이터 조회에서 데이터가 조회 되지 않으면 예외가 발생한다.")
    @Test
    void queryForObject3() throws SQLException {
        String sql = "SELECT id FROM users";

        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("name")
        ), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("EmptyResultDataAccessException");
    }
}
