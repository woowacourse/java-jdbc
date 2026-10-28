package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_ROW_MAPPER = (resultSet, rowNum) -> resultSet.getString("name");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_인자를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int affectedRows = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setObject(2, 1L);
    }

    @Test
    void update가_끝나면_PreparedStatement와_Connection을_닫는다() throws SQLException {
        jdbcTemplate.update("delete from users");

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void SQLException이_발생하면_자원을_닫고_DataAccessException을_던진다() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("syntax error"));

        assertThatThrownBy(() -> jdbcTemplate.update("wrong sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void query는_모든_행을_RowMapper로_변환하고_ResultSet을_닫는다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "tinu");

        final List<String> names = jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        assertThat(names).containsExactly("gugu", "tinu");
        verify(resultSet).close();
    }

    @Test
    void queryForObject는_한_행이면_그_결과를_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("gugu");

        final String name = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(name).isEqualTo("gugu");
        verify(preparedStatement).setObject(1, 1L);
    }

    @Test
    void queryForObject는_결과가_없으면_EmptyResultDataAccessException을_던진다() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject는_결과가_두_행_이상이면_IncorrectResultSizeDataAccessException을_던진다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where account = ?", NAME_ROW_MAPPER, "gugu"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void 직접_지정한_바인더로_SQL_타입을_지정한다() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int affectedRows = jdbcTemplate.update("update users set name = ?", statement ->
                statement.setNull(1, Types.VARCHAR));

        assertThat(affectedRows).isEqualTo(1);
        verify(preparedStatement).setNull(1, Types.VARCHAR);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void 결과_추출기로_여러_행을_하나의_값으로_집계한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("amount")).thenReturn(10, 20);

        final int sum = jdbcTemplate.query("select amount from orders where user_id = ?",
                statement -> statement.setLong(1, 42L), results -> {
                    int total = 0;
                    while (results.next()) {
                        total += results.getInt("amount");
                    }
                    return total;
                });

        assertThat(sum).isEqualTo(30);
        verify(preparedStatement).setLong(1, 42L);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void 바인딩에_실패하면_원인을_보존하고_자원을_닫는다() throws SQLException {
        final var failure = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set name = ?", statement -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void 결과_추출에_실패하면_원인을_보존하고_모든_자원을_닫는다() throws SQLException {
        final var failure = new SQLException("extraction failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select name from users", statement -> {}, results -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void 매퍼의_런타임_예외를_그대로_전달하고_모든_자원을_닫는다() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new IllegalArgumentException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select name from users", (results, rowNum) -> {
            throw failure;
        })).isSameAs(failure);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
