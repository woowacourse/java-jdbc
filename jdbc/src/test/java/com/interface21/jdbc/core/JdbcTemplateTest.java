package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private static final RowMapper<User> ROW_MAPPER = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account")
            );

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ParameterMetaData parameterMetaData;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        parameterMetaData = mock(ParameterMetaData.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("업데이트 쿼리 실행이 성공한다.")
    void update() throws SQLException {
        //given
        String sql = "update users set account = ? where id = ?";
        when(parameterMetaData.getParameterCount()).thenReturn(2);

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
        when(parameterMetaData.getParameterCount()).thenReturn(2);

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
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("test-ash");

        //when
        Optional<User> user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, 1L);

        //then
        assertAll(
                () -> assertThat(user).isPresent(),
                () -> assertThat(user.get().id).isEqualTo(1L),
                () -> assertThat(user.get().account).isEqualTo("test-ash"),
                this::verifyQueryResourcesClosed
        );
    }

    @Test
    @DisplayName("단건 조희 쿼리 실행은 성공했지만 해당 값이 없다.")
    void queryForObject_fail_noSuchElement() throws SQLException {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(resultSet.next()).thenReturn(false);

        //when
        Optional<User> user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, 1L);

        //then
        assertAll(
                () -> assertThat(user).isNotPresent(),
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

    private void verifyQueryResourcesClosed() {
        assertAll(
                () -> verify(resultSet).close(),
                this::verifyConnectionClosed
        );
    }

    private void verifyConnectionClosed() {
        assertAll(
                () -> verify(preparedStatement).close()
        );
    }

    static class User {
        private final Long id;
        private final String account;

        public User(Long id, String account) {
            this.id = id;
            this.account = account;
        }
    }
}
