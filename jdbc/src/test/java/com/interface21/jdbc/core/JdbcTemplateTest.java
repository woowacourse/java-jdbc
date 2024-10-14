package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @DisplayName("데이터를 변경하는 update 메서드는 몇 개의 행이 영향을 받았는지 알 수 있다.")
    @Test
    void update() throws SQLException {
        String sql = "update users set password = ?, email = ? WHERE id = ?";
        Object[] args = new Object[]{"password", "email", 1};

        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = jdbcTemplate.update(sql, args);
        assertThat(result).isEqualTo(1);

        verify(preparedStatement).setObject(1, "password");
        verify(preparedStatement).setObject(2, "email");
        verify(preparedStatement).setObject(3, 1);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("데이터를 조회하는 query 메서드는 여러 개의 행을 조회할 수 있다.")
    @Test
    void query() throws SQLException {
        String sql = "select * from users";
        TestUser testUser1 = new TestUser(1, "account1", "password1", "email1");
        TestUser testUser2 = new TestUser(2, "account2", "password2", "email2");
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(testUser1, testUser2);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<TestUser> results = jdbcTemplate.query(sql, rowMapper);
        assertAll(
                () -> assertThat(results.size()).isEqualTo(2),
                () -> assertThat(testUser1).isEqualTo(results.get(0)),
                () -> assertThat(testUser2).isEqualTo(results.get(1))
        );

        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();
        verify(rowMapper, times(2)).mapRow(resultSet);
    }

    @DisplayName("데이터를 조회하는 query 메서드는 조건에 따라 여러 개의 행을 조회할 수 있다.")
    @Test
    void queryWithArgs() throws SQLException {
        String sql = "select * from users where id = ?";
        TestUser testUser1 = new TestUser(1, "account1", "password1", "email1");
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(testUser1);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<TestUser> results = jdbcTemplate.query(sql, new Object[]{1}, rowMapper);
        assertAll(
                () -> assertThat(results.size()).isEqualTo(1),
                () -> assertThat(testUser1).isEqualTo(results.get(0))
        );

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(2)).next();
        verify(rowMapper, times(1)).mapRow(resultSet);
    }

    @DisplayName("데이터를 조회하는 queryForObject 메서드는 단일 행을 조회할 수 있다.")
    @Test
    void queryForObject() throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        TestUser testUser = new TestUser(1, "account1", "password1", "email1");
        Object[] args = {1};
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn(testUser);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        TestUser result = jdbcTemplate.queryForObject(sql, args, rowMapper);
        assertThat(testUser).isEqualTo(result);

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).executeQuery();
        verify(rowMapper).mapRow(resultSet);
    }

    @DisplayName("데이터를 조회하는 queryForObject 메서드는 조회된 행이 없는 경우 예외가 발생한다.")
    @Test
    void queryForObjectEmptyQueryResult() throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        Object[] args = {1};
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, args, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단일 행 조회를 기대했지만, 조회된 행이 없습니다.");

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
    }

    @DisplayName("데이터를 조회하는 queryForObject 메서드는 여러 행이 조회된 경우 예외가 발생한다.")
    @Test
    void queryForObjectMultipleResults() throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        Object[] args = {1};
        RowMapper<TestUser> rowMapper = mock(RowMapper.class);

        when(resultSet.next()).thenReturn(true, true);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, args, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단일 행 조회를 기대했지만, 여러 행이 조회되었습니다.");

        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(2)).next();
    }
}
