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

    private static RowMapper<User> ROW_MAPPER = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account")
            );

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verifyConnectionClosed();
    }

    private void verifyConnectionClosed() throws SQLException {
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("업데이트 쿼리 실행이 성공한다.")
    void update() {
        //given
        String sql = "update users set account = ? where id = ?";

        //when
        jdbcTemplate.update(sql, "test-ash", 1L);

        //then
        assertAll(
                () -> verify(preparedStatement).setObject(1, "test-ash"),
                () -> verify(preparedStatement).setObject(2, 1L),
                () -> verify(preparedStatement).executeUpdate(),
                this::verifyConnectionClosed
        );
    }

    @Test
    @DisplayName("업데이트 쿼리 실행이 실패한다.")
    void update_fail() throws SQLException {
        //given
        String sql = "update users set account = ? where id = ?";
        doThrow(new SQLException("에러 테스트")).when(preparedStatement).executeUpdate();

        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql, "test-ash", 1L))
                        .isExactlyInstanceOf(DataAccessException.class)
                        .hasMessageContaining("에러 테스트"),
                this::verifyConnectionClosed
        );
    }

    @Test
    @DisplayName("단건 조희 쿼리 실행이 성공한다.")
    void queryForObject() throws SQLException {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("test-ash");

        //when
        User user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, 1L);

        //then
        assertAll(
                () -> assertThat(user.id).isEqualTo(1L),
                () -> assertThat(user.account).isEqualTo("test-ash"),
                this::verifyQueryResourcesClosed
        );
    }

    @Test
    @DisplayName("단건 조희 쿼리 실행은 성공했지만 해당 값이 없다.")
    void queryForObject_fail_noSuchElement() throws SQLException {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        //when
        User user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, 1L);

        //then
        assertAll(
                () -> assertNull(user),
                this::verifyQueryResourcesClosed
        );
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
        List<User> users = jdbcTemplate.query(sql, ROW_MAPPER);

        //then
        assertAll(
                () -> assertThat(users.getFirst().account).isEqualTo("test"),
                () -> assertThat(users.getLast().account).isEqualTo("ash"),
                () -> verify(resultSet, times(3)).next(),
                () -> verify(resultSet, times(2)).getLong("id"),
                this::verifyQueryResourcesClosed
        );
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
