package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JdbcTemplateTest {

    RowMapper<User> mapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    @Mock
    DataSource dataSource;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @InjectMocks
    JdbcTemplate jdbcTemplate;
    @Mock
    ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("하나의 데이터를 추가하는 쿼리문을 실행한다.")
    void update() throws SQLException {
        // given
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        String account = "seyang";
        String password = "password";
        String email = "seyang@naver.com";

        // when
        when(preparedStatement.executeUpdate()).thenReturn(1);
        int rowsAffected = jdbcTemplate.update(sql, account, password, email);

        // then
        assertThat(rowsAffected).isEqualTo(1);
        verify(preparedStatement).setObject(1, account);
        verify(preparedStatement).setObject(2, password);
        verify(preparedStatement).setObject(3, email);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("모든 데이터를 조회하는 쿼리를 실행한다.")
    void query() throws SQLException {
        // given
        String sql = "SELECT * FROM users";
        List<User> users = List.of(
                new User(1L, "seyang", "yang", "yang@ygh.kr"),
                new User(2L, "seho", "ho", "ho@ygh.kr")
        );

        // when
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id"))
                .thenReturn(users.getFirst().id(), users.getLast().id());
        when(resultSet.getString("account"))
                .thenReturn(users.getFirst().account(), users.getLast().account());
        when(resultSet.getString("password"))
                .thenReturn(users.getFirst().password(), users.getLast().password());
        when(resultSet.getString("email"))
                .thenReturn(users.getFirst().email(), users.getLast().email());
        List<User> actual = jdbcTemplate.query(sql, mapper);

        // then
        assertThat(actual).isEqualTo(users);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(users.size() + 1)).next();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("특정된 하나의 데이터에 대해 조회한다.")
    void queryForObject() throws SQLException, IncorrectResultSizeDataAccessException {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = new User(2L, "seho", "ho", "ho@ygh.kr");

        // when
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(user.id());
        when(resultSet.getString("account")).thenReturn(user.account());
        when(resultSet.getString("password")).thenReturn(user.password());
        when(resultSet.getString("email")).thenReturn(user.email());
        Optional<User> actual = jdbcTemplate.queryForObject(sql, mapper, user.id());

        // then
        assertThat(actual).hasValue(user);
        verify(preparedStatement).setObject(1, user.id());
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("하나의 데이터를 조회할 때 쿼리의 결과가 다수일 경우 예외가 발생한다.")
    void queryForObjectWithManyResults() throws Exception {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        when(resultSet.next()).thenReturn(true, true, false);

        // then
        assertThatCode(() -> jdbcTemplate.queryForObject(sql, mapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    record User(Long id, String account, String password, String email) {
    }
}
