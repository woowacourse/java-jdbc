package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final RowMapper<String> rowMapper = rs -> rs.getString("account");

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update_ThenBindParametersInOrderAndExecute() throws SQLException {
        String sql = "insert into users (account, password) values (?, ?)";
        String account = "gugu";
        String password = "password";

        jdbcTemplate.update(sql, account, password);

        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "gugu");
        inOrder.verify(preparedStatement).setObject(2, "password");
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void update_WhenPreparedStatementSetter_ThenBindBySetter() throws SQLException {
        String sql = "insert into users (account, password) values (?, ?)";

        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, "gugu");
            ps.setString(2, "password");
        });

        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setString(1, "gugu");
        inOrder.verify(preparedStatement).setString(2, "password");
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void update_WhenSetterThrowsSQLException_ThenThrowDataAccessExceptionAndCloseResources() throws SQLException {
        String sql = "insert into users (account) values (?)";
        final SQLException cause = new SQLException("bind failed");

        assertThatThrownBy(() -> jdbcTemplate.update(sql, ps -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement, never()).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void query_WhenNoArgs_ThenNotBindAnyParameter() throws SQLException {
        String sql = "select account from users";

        jdbcTemplate.query(sql, rowMapper);

        verify(preparedStatement).executeQuery();
        verify(preparedStatement, never()).setObject(anyInt(), any());
    }

    @Test
    void query_ThenCloseResourcesInReverseOrder() throws SQLException {
        String sql = "select 1 from users";

        jdbcTemplate.query(sql, rowMapper);

        final InOrder inOrder = inOrder(resultSet, preparedStatement, connection);
        inOrder.verify(resultSet).close();
        inOrder.verify(preparedStatement).close();
        inOrder.verify(connection).close();
    }

    @Test
    void queryForObject_WhenSingleRow_ThenReturnIt() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");
        String sql = "select account from users where id = ?";

        final String result = jdbcTemplate.queryForObject(sql, rowMapper, 1L);

        assertThat(result).isEqualTo("gugu");
        verify(preparedStatement).setObject(1, 1L);
    }

    @Test
    void queryForObject_WhenPreparedStatementSetter_ThenReturnSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");
        String sql = "select account from users where id = ?";

        final String result = jdbcTemplate.queryForObject(sql, rowMapper, ps -> ps.setLong(1, 1L));

        assertThat(result).isEqualTo("gugu");
        verify(preparedStatement).setLong(1, 1L);
    }

    @Test
    void queryForObject_WhenEmptyRow_ThrowEmptyResultDataAccessException() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        String sql = "select account from users where id = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject_WhenMultipleRow_ThrowIncorrectResultSizeDataAccessException() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        String sql = "select account from users where id = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
