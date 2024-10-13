package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    record User(long id, String account, String password, String email) {}

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("주어진 connection을 이용하여 update 메서드를 실행하면 조작된 행 개수를 반환한다.")
    @Test
    void updateWithConnection() throws SQLException {
        String sql = "UPDATE users SET account = ? WHERE id = ?";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int actual = jdbcTemplate.update(sql, connection, "mark", 1L);

        assertEquals(1, actual);
        verify(preparedStatement).setObject(1, "mark");
        verify(preparedStatement).executeUpdate();
    }


    @DisplayName("update 메서드를 실행하면 조작된 행 개수를 반환한다.")
    @Test
    void update() throws SQLException {
        String sql = "UPDATE users SET account = ? WHERE id = ?";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int actual = jdbcTemplate.update(sql, "mark", 1L);

        assertEquals(1, actual);
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).setObject(1, "mark");
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("query 메서드를 실행하면 생성된 객체를 반환한다.")
    @Test
    void query() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("mark");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("email")).thenReturn("asd@asd.com");
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, 1L);

        assertAll(
                () -> assertEquals(1, users.size()),
                () -> assertEquals(1L, users.get(0).id()),
                () -> assertEquals("mark", users.get(0).account()),
                () -> assertEquals("password", users.get(0).password())

        );
        verify(preparedStatement).executeQuery();
    }

    @DisplayName("queryForObject 메서드를 실행하면 생성된 객체 하나를 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("mark");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("email")).thenReturn("asd@asd.com");
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        User user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, 1L);

        assertAll(
                () -> assertEquals(1L, user.id()),
                () -> assertEquals("mark", user.account()),
                () -> assertEquals("password", user.password())

        );
        verify(preparedStatement).executeQuery();
    }

    @DisplayName("queryForObject 메서드를 실행했는데 결과가 없으면 예외가 발생한다.")
    @Test
    void queryForObjectWithEmptyResult() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(DataAccessException.class, () -> jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, 1L));
        verify(preparedStatement).executeQuery();
    }

    @DisplayName("queryForObject 메서드를 실행했는데 결과가 2개 이상이면 예외가 발생한다.")
    @Test
    void queryForObjectWithMultipleResults() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        assertThrows(DataAccessException.class, () -> jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, 1L));
        verify(preparedStatement).executeQuery();
    }
}
