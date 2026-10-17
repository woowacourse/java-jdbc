package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    void queryForObject() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        String sql = "select id from users where account = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        RowMapper<Integer> rowMapper = rs -> rs.getInt("id");

        // when & then
        assertThatThrownBy(
                () -> jdbcTemplate.queryForObject(sql, rowMapper, "gugu")
        ).isInstanceOf(DataAccessException.class);
    }

    @Test
    void update() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        String sql = "update users set password = ? where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        int affectedRows = jdbcTemplate.update(sql, "newpassword", 1L);

        // then
        assertThat(affectedRows).isEqualTo(1);

        verify(pstmt).setObject(1, "newpassword");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).close();
        verify(connection).close();
    }
}
