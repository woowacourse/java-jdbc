package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private static final String SQL = "select name from members where id = ?";
    private static final RowMapper<String> NAME_MAPPER = resultSet -> resultSet.getString("name");

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("update: 가변인자를 순서대로 바인딩하고 영향받은 행 수를 반환한다")
    void updateWithVarargs() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update(SQL, "gugu", 10L, "email");

        assertThat(affected).isEqualTo(1);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 10L);
        verify(statement).setObject(3, "email");
    }

    @Test
    @DisplayName("update: PreparedStatementSetter로 값을 설정할 수 있다")
    void updateWithSetter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update(SQL, stmt -> stmt.setString(1, "gugu"));

        assertThat(affected).isEqualTo(1);
        verify(statement).setString(1, "gugu");
    }

    @Test
    @DisplayName("select: 모든 행을 RowMapper로 매핑해 반환한다")
    void select() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("a", "b");

        final List<String> names = jdbcTemplate.select(SQL, NAME_MAPPER, 1L);

        assertThat(names).containsExactly("a", "b");
        verify(statement).setObject(1, 1L);
    }

    @Test
    @DisplayName("select: 결과가 없으면 빈 리스트를 반환한다")
    void selectEmpty() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final List<String> names = jdbcTemplate.select(SQL, NAME_MAPPER, 1L);

        assertThat(names).isEmpty();
    }

    @Test
    @DisplayName("selectOne: 한 행만 반환한다")
    void selectOne() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("a");

        final String name = jdbcTemplate.selectOne(SQL, NAME_MAPPER, 1L);

        assertThat(name).isEqualTo("a");
    }

    @Test
    @DisplayName("selectOne: 결과가 없으면 예외를 던진다")
    void selectOneEmpty() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.selectOne(SQL, NAME_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("found 0");
    }

    @Test
    @DisplayName("selectOne: 결과가 여러 행이면 예외를 던진다")
    void selectOneMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("a", "b");

        assertThatThrownBy(() -> jdbcTemplate.selectOne(SQL, NAME_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("found 2");
    }

    @Test
    @DisplayName("update: SQLException은 DataAccessException으로 변환된다")
    void updateThrowsDataAccessException() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("fail"));

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("select: SQLException은 DataAccessException으로 변환된다")
    void selectThrowsDataAccessException() throws SQLException {
        when(statement.executeQuery()).thenThrow(new SQLException("fail"));

        assertThatThrownBy(() -> jdbcTemplate.select(SQL, NAME_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("select: 실행 후 ResultSet, Statement, Connection을 순서대로 닫는다")
    void selectClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        jdbcTemplate.select(SQL, NAME_MAPPER, 1L);

        final InOrder inOrder = inOrder(resultSet, statement, connection);
        inOrder.verify(resultSet).close();
        inOrder.verify(statement).close();
        inOrder.verify(connection).close();
    }

    @Test
    void selectReusesTransactionConnectionUntilUnbound() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            jdbcTemplate.select(SQL, NAME_MAPPER, 1L);

            verify(dataSource, never()).getConnection();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        verify(connection).close();
    }

    @Test
    @DisplayName("update: 예외가 발생해도 Statement, Connection을 닫는다")
    void updateClosesResourcesOnFailure() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("fail"));

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(statement).close();
        verify(connection).close();
    }
}
