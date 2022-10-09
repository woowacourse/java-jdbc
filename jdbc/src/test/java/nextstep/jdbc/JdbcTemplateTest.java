package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private DataSourceTransactionManager transactionManager;
    private TransactionStatus transactionStatus;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.statement = mock(PreparedStatement.class);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(statement);
        this.transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @AfterEach
    void tearDown() {
        transactionManager.rollback(transactionStatus);

        assertAll(
            () -> verify(statement).close(),
            () -> verify(connection).close()
        );
    }

    @Test
    void SELECT를_실행했을_때_하나의_객체_결과_값을_도출할_수_있다() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("rookie");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * from users where id = 1";

        // when
        jdbcTemplate.queryForObject(sql, mock(RowMapper.class));

        // then
        assertAll(
            () -> verify(dataSource).getConnection(),
            () -> verify(connection).prepareStatement(anyString()),
            () -> verify(resultSet).close()
        );
    }

    @Test
    void SELECT를_실행했을_때_여러개의_객체_결과_값을_도출할_수_있다() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("rookie");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * from users";

        // when
        jdbcTemplate.queryForList(sql, mock(RowMapper.class));

        // then
        assertAll(
            () -> verify(dataSource).getConnection(),
            () -> verify(connection).prepareStatement(anyString()),
            () -> verify(resultSet).close()
        );
    }

    @Test
    void UPDATE를_실행했을_때_변환_ROW값을_반환할_수_있다() throws SQLException {
        // given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "INSERT INTO users VALUES(?, ?, ?)";

        // when
        int actual = jdbcTemplate.update(sql, 1, 2, 3);

        // then
        assertAll(
            () -> assertThat(actual).isEqualTo(1),
            () -> verify(dataSource).getConnection(),
            () -> verify(connection).prepareStatement(anyString())
        );
    }
}
