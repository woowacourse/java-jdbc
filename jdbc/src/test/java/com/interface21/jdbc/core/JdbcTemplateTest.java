package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    void 여러_행을_조회하여_변환한다() throws SQLException {
        // given
        DataSource dataSource= mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select account from users";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        List<String> accounts = jdbcTemplate.query(sql, rs -> rs.getString("account"));

        // then
        assertThat(accounts).containsExactly("gugu", "brown");
    }

    @Test
    void 조회_결과가_없으면_빈_목록을_반환한다() throws SQLException {
        // given
        DataSource dataSource= mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select account from users";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        List<String> accounts = jdbcTemplate.query(sql, rs -> rs.getString("account"));

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    void 인자를_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        String sql = "update users set password = ? where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        int affectedRows = jdbcTemplate.update(sql, "new-password", 1L);

        // then
        assertThat(affectedRows).isEqualTo(1);

        var order = inOrder(statement);
        order.verify(statement).setObject(1, "new-password");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
    }

    @Test
    void SQL_예외를_데이터_접근_예외로_변환한다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        String sql = "delete from users where id = ?";
        SQLException cause = new SQLException("SQL 실행 실패");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void 행_변환에_실패해도_자원을_닫는다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select account from users";
        SQLException cause = new SQLException("행 변환 실패");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when & then
        assertThatThrownBy(() ->
                jdbcTemplate.query(sql, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 단건_조회_결과를_객체로_반환한다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select account from users where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        String account = jdbcTemplate.queryForObject(
                sql, rs -> rs.getString("account"), 1L);

        // then
        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select account from users where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        String account = jdbcTemplate.queryForObject(
                sql, rs -> rs.getString("account"), 999L);

        // then
        assertThat(account).isNull();
    }
}
