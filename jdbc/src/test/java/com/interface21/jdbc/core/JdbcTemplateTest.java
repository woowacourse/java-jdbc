package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

    private static final String SELECT_SQL = "select account from users where id = ?";
    private static final String INSERT_SQL = "insert into users (account) values (?)";
    private static final RowMapper<String> ACCOUNT_ROW_MAPPER = (rs, rowNum) -> rs.getString("account");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForOptional_결과가_없으면_빈_Optional() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        assertThat(actual).isEmpty();
    }

    @Test
    void queryForOptional_결과가_1건이면_값을_담은_Optional() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var actual = jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        assertThat(actual).contains("gugu");
    }

    @Test
    void queryForOptional_결과가_2건_이상이면_IncorrectResultSizeDataAccessException() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .satisfies(e -> assertThat(((IncorrectResultSizeDataAccessException) e).getActualSize()).isEqualTo(2));
    }

    @Test
    void SQLException이_발생하면_DataAccessException으로_변환() throws SQLException {
        final var sqlException = new SQLException("syntax error");
        when(connection.prepareStatement(anyString())).thenThrow(sqlException);

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(sqlException);
    }

    @Test
    void 예외가_발생해도_자원을_닫는다() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException("connection lost"));

        assertThatThrownBy(() -> jdbcTemplate.queryForOptional(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
