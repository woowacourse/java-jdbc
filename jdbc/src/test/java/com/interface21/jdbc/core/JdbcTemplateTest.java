package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObject_결과가_없으면_예외를_던진다() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select id from users where id = ?",
                row -> row.getLong("id"),
                1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject_결과가_2건_이상이면_예외를_던진다() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select id from users where account = ?",
                row -> row.getLong("id"),
                "gugu"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void query_여러_행을_RowMapper로_매핑한다() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString(1)).thenReturn("gugu", "woowa");

        final List<String> results = jdbcTemplate.query(
                "select account from users",
                row -> row.getString(1));

        assertThat(results).containsExactly("gugu", "woowa");
    }

    @Test
    void update_파라미터가_순서대로_바인딩된다() throws SQLException {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                "gugu", "password", "hkkang@woowahan.com");

        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, "password");
        verify(pstmt).setObject(3, "hkkang@woowahan.com");
        verify(pstmt).executeUpdate();
    }

    @Test
    void 트랜잭션이_진행_중이면_바인딩된_커넥션을_재사용하고_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            jdbcTemplate.update("update users set password = ? where id = ?", "newPassword", 1L);

            verify(pstmt).executeUpdate();
            verify(pstmt).close();
            verify(connection, never()).close();
            verify(dataSource, never()).getConnection();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 트랜잭션_진행_중_SQL_실행이_실패해도_커넥션을_닫지_않는다() throws SQLException {
        when(pstmt.executeUpdate()).thenThrow(new SQLException("execution error"));
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> jdbcTemplate.update(
                    "update users set password = ? where id = ?", "newPassword", 1L))
                    .isInstanceOf(DataAccessException.class);

            verify(pstmt).close();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void SQLException이_발생하면_DataAccessException으로_래핑한다() throws SQLException {
        when(pstmt.executeQuery()).thenThrow(new SQLException("connection error"));

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select id from users",
                row -> row.getLong("id")))
                .isInstanceOf(DataAccessException.class);
    }
}