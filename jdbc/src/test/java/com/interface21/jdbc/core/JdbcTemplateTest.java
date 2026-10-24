package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final RowMapper<String> ACCOUNT_MAPPER = rs -> rs.getString("account");

    private Connection connection;
    private PreparedStatement pstmt;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        when(pstmt.executeUpdate()).thenReturn(1);

        final var actual = jdbcTemplate.update("update users set password = ? where id = ?", "password", 1L);

        assertThat(actual).isEqualTo(1);
        verify(pstmt).setObject(1, "password");
        verify(pstmt).setObject(2, 1L);
    }

    @Test
    void updateWithPreparedStatementSetter() throws SQLException {
        jdbcTemplate.update("update users set password = ? where id = ?", pstmt -> {
            pstmt.setString(1, "password");
            pstmt.setLong(2, 1L);
        });

        verify(pstmt).setString(1, "password");
        verify(pstmt).setLong(2, 1L);
        verify(pstmt).executeUpdate();
    }

    @Test
    void updateClosesResources() throws SQLException {
        jdbcTemplate.update("update users set password = ? where id = ?", "password", 1L);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void updateThrowsDataAccessException() throws SQLException {
        when(pstmt.executeUpdate()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "secret"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("update users set password = ?")
                .hasMessageNotContaining("secret");
    }

    @Test
    void query() throws SQLException {
        final var rs = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "kyle");

        final var actual = jdbcTemplate.query("select account from users", ACCOUNT_MAPPER);

        assertThat(actual).containsExactly("gugu", "kyle");
        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void queryWithPreparedStatementSetter() throws SQLException {
        final var rs = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("account")).thenReturn("gugu");

        final var actual = jdbcTemplate.query("select account from users where id = ?", ACCOUNT_MAPPER,
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(actual).containsExactly("gugu");
        verify(pstmt).setLong(1, 1L);
    }

    @Test
    void queryForObject() throws SQLException {
        final var rs = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("account")).thenReturn("gugu");

        final var actual = jdbcTemplate.queryForObject("select account from users where id = ?", ACCOUNT_MAPPER, 1L);

        assertThat(actual).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void queryForObjectReturnsNullWhenEmpty() throws SQLException {
        final var rs = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForObject("select account from users where id = ?", ACCOUNT_MAPPER, 1L);

        assertThat(actual).isNull();
    }

    @Test
    void queryForObjectThrowsWhenMoreThanOne() throws SQLException {
        final var rs = mock(ResultSet.class);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", ACCOUNT_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("select account from users");
    }
}
