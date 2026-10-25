package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String SQL = "select name from t where id = ?";

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
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_인자를_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update(SQL, 1L, "a");

        assertThat(affected).isEqualTo(1);
        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "a");
        verify(connection).close();
    }

    @Test
    void query는_모든_행을_RowMapper로_변환한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("a", "b");

        final List<String> names = jdbcTemplate.query(SQL, rs -> rs.getString("name"), 1L);

        assertThat(names).containsExactly("a", "b");
        verify(resultSet).close();
    }

    @Test
    void queryForObject는_결과가_없으면_빈_Optional을_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.queryForObject(SQL, rs -> rs.getString("name"), 1L)).isEmpty();
    }

    @Test
    void queryForObject는_결과가_하나면_그_값을_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("a");

        assertThat(jdbcTemplate.queryForObject(SQL, rs -> rs.getString("name"), 1L)).contains("a");
    }

    @Test
    void queryForObject는_결과가_여러_건이면_예외를_던진다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("a", "b");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("name"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("2건")
                .hasMessageContaining(SQL);
    }

    @Test
    void SQLException은_DataAccessException으로_변환하고_커넥션을_닫는다() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("boom"));

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseInstanceOf(SQLException.class);
        verify(connection).close();
    }

    @Test
    void 트랜잭션_중이면_묶인_커넥션을_쓰고_닫지_않는다() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            jdbcTemplate.update(SQL, 1L);

            verify(connection, never()).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 트랜잭션_밖이면_사용_후_스레드에서_떼고_닫는다() throws SQLException {
        jdbcTemplate.update(SQL, 1L);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }
}
