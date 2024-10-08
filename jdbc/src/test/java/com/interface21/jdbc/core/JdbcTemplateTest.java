package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private static RowMapper<User> rowMapper = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account")
            );

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("업데이트 쿼리 실행이 성공한다.")
    void update() throws SQLException {
        //given
        String sql = "update users set account = ? where id = ?";

        //when
        jdbcTemplate.update(sql, "test-ash", 1L);

        //then
        verify(preparedStatement).setObject(1, "test-ash");
        verify(preparedStatement).setObject(2, 1L);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("업데이트 쿼리 실행이 실패한다.")
    void update_fail() throws SQLException {
        //given
        String sql = "update users set account = ? where id = ?";
        doThrow(new SQLException("에러 테스트")).when(preparedStatement).executeUpdate();

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "test-ash", 1L))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessageContaining("에러 테스트");
    }

    @Test
    @DisplayName("단건 조희 쿼리 실행이 성공한다.")
    void queryForObject() throws SQLException {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("test-ash");

        //when
        User user = jdbcTemplate.queryForObject(sql, rowMapper, 1L);

        //then
        assertAll(
                () -> assertThat(user.id).isEqualTo(1L),
                () -> assertThat(user.account).isEqualTo("test-ash")
        );

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조희 쿼리 실행은 성공했지만 해당 값이 없다.")
    void queryForObject_fail_noSuchElement() throws SQLException {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        //when
        User user = jdbcTemplate.queryForObject(sql, rowMapper, 1L);

        //then
        assertNull(user);

        verify(resultSet).close();
    }

    @Test
    @DisplayName("다수건 조회 쿼리 실행이 성공한다.")
    void query() throws SQLException {
        //given
        String sql = "select * from users";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("account")).thenReturn("test", "ash");

        //when
        List<User> users = jdbcTemplate.query(sql, rowMapper);

        //then
        assertAll(
                () -> assertThat(users.getFirst().account).isEqualTo("test"),
                () -> assertThat(users.getLast().account).isEqualTo("ash")
        );

        verify(resultSet, times(3)).next();
        verify(resultSet, times(2)).getLong("id");
        verify(resultSet).close();
    }

    static class User {
        private Long id;
        private String account;

        public User(Long id, String account) {
            this.id = id;
            this.account = account;
        }
    }
}
