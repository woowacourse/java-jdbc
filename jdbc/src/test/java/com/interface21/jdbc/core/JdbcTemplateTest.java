package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("update 메서드를 사용하여 커맨드성 쿼리 실행")
    void commandTest() throws SQLException {
        final String sql = "UPDATE users SET account = ?, email = ? WHERE id = ?";

        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int rowsAffected = jdbcTemplate.update(sql, "libi", "libi@example.com", 1L);
        assertThat(rowsAffected).isEqualTo(1);

        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "libi");
        verify(preparedStatement).setObject(2, "libi@example.com");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("update 메서드를 이용해서 데이터 영속화")
    void insertWithGeneratedKey() throws SQLException {
        final String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(100L);

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final int rowsAffected = jdbcTemplate.update(sql, keyHolder, "mark", "mark@example.com");

        Assertions.assertAll(
                () -> assertThat(rowsAffected).isEqualTo(1),
                () -> assertThat(keyHolder.getKey()).isEqualTo(100L)
        );

        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "mark");
        verify(preparedStatement).setObject(2, "mark@example.com");
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
        verify(resultSet).next();
        verify(resultSet).getObject(1);
    }

    @Test
    @DisplayName("query 메서드를 통해 데이터 리스트 반환")
    void queryReturnsList() throws SQLException {
        final String sql = "SELECT id, name, email FROM users";

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("libi", "mark");
        when(resultSet.getString("email")).thenReturn("libi@example.com", "mark@example.com");

        final List<User> users = jdbcTemplate.query(sql, new UserRowMapper());

        assertThat(users).hasSize(2);

        final User firstUser = users.getFirst();
        final User secondUser = users.get(1);

        Assertions.assertAll(
                () -> assertThat(firstUser.getId()).isEqualTo(1L),
                () -> assertThat(firstUser.getName()).isEqualTo("libi"),
                () -> assertThat(firstUser.getEmail()).isEqualTo("libi@example.com"),
                () -> assertThat(secondUser.getId()).isEqualTo(2L),
                () -> assertThat(secondUser.getName()).isEqualTo("mark"),
                () -> assertThat(secondUser.getEmail()).isEqualTo("mark@example.com")
        );
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
    }

    @Test
    @DisplayName("queryForObject 메서드로 단건 조회")
    void queryForObject() throws SQLException {
        final String sql = "SELECT id, name, email FROM users WHERE id = ?";

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("libi");
        when(resultSet.getString("email")).thenReturn("libi@example.com");

        final User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), 1L);

        Assertions.assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getName()).isEqualTo("libi"),
                () -> assertThat(user.getEmail()).isEqualTo("libi@example.com")
        );

        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, 1L);
        verify(preparedStatement).executeQuery();
    }

    @Test
    @DisplayName("단건 조회시 데이터가 존재하지 않을 경우 null 반환")
    void queryForObjectNotFound() throws SQLException {
        final String sql = "SELECT id, name, email FROM users WHERE email = ?";

        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, new UserRowMapper(), "notfound@example.com"))
                .isInstanceOf(EmptyResultDataAccessException.class);

        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "notfound@example.com");
        verify(preparedStatement).executeQuery();
    }

    @Test
    @DisplayName("queryForObject 메서드의 결과가 단건이 아닌 경우 예외 발생")
    void queryForObjectMultipleResults() throws SQLException {
        final String sql = "SELECT id, name, email FROM users WHERE email = ?";

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("libi", "mark");
        when(resultSet.getString("email")).thenReturn("libi@example.com", "mark@example.com");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, new UserRowMapper(), "libi@example.com"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);

        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "libi@example.com");
        verify(preparedStatement).executeQuery();
    }


    static class User {
        private Long id;
        private String name;
        private String email;

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String email = rs.getString("email");
            return new User(id, name, email);
        }
    }
}
