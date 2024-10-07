package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    void queryForObject는_Result값이_1개보다_많으면_예외가_발생한다() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        PreparedStatement pstmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(pstmt);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        RowMapper rowMapper = mock(RowMapper.class);
        when(rowMapper.mapRow(resultSet, 1)).thenReturn(new Object());
        when(rowMapper.mapRow(resultSet, 2)).thenReturn(new Object());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT * FROM table", rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void queryForObject는_Result값이_0개면_예외가_발생한다() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        PreparedStatement pstmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(pstmt);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        RowMapper rowMapper = mock(RowMapper.class);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT * FROM table", rowMapper))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
