package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 모든_조회_행을_객체로_변환한다() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "lucky");

        final var accounts = jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        );

        assertThat(accounts).containsExactly("gugu", "lucky");

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        final String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                1L
        );

        assertThat(account).isNull();
    }

    @Test
    void 수정할_값을_SQL_매개변수_순서대로_설정한다() throws SQLException {
        when(pstmt.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "new-password", 1L
        );

        assertThat(updatedRows).isEqualTo(1);
        final var executionOrder = inOrder(pstmt);
        executionOrder.verify(pstmt).setObject(1, "new-password");
        executionOrder.verify(pstmt).setObject(2, 1L);
        executionOrder.verify(pstmt).executeUpdate();
    }

    @Test
    void 매개변수_설정에_실패하면_예외를_변환하고_자원을_해제한다() throws SQLException {
        final SQLException failure = new SQLException("binding failed");
        doThrow(failure).when(pstmt).setObject(1, "new-password");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set password = ? where id = ?", "new-password", 1L
        ))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(pstmt, never()).executeUpdate();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 수정_실행에_실패하면_자원을_해제한다() throws SQLException {
        final SQLException failure = new SQLException("update failed");
        when(pstmt.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set password = ? where id = ?", "new-password", 1L
        ))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 조회_결과_변환에_실패하면_자원을_해제한다() throws SQLException {
        final SQLException failure = new SQLException("mapping failed");
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        final RowMapper<String> rowMapper = row -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }
}
