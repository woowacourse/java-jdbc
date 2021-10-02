package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DatabaseConnectionFailureException;
import nextstep.jdbc.exception.PreparedStatementCreationFailureException;
import nextstep.jdbc.exception.QueryExecutionFailureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @DisplayName("update() 사용 후 커넥션이 닫힌다")
    @Test
    void closeConnectionAfterUpdate() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select * from hyeon9mak";
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(pstmt);

        // when
        jdbcTemplate.update(sql, "999", "let's eat");

        // then
        verify(conn).close();
        verify(pstmt).close();
    }

    @DisplayName("Connection을 가져오는 것에 실패할 경우 DataConnectionFailureException을 던진다")
    @Test
    void whenConnectionFailure_throwDataConnectionFailureException() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select * from hyeon9mak";
        when(dataSource.getConnection()).thenThrow(new SQLException());

        // when // then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "hyeon9mak best"))
            .isExactlyInstanceOf(DatabaseConnectionFailureException.class);
    }

    @DisplayName("PreparedStatement 형성에 실패할 경우 PreparedStatementCreationFailureException을 던진다")
    @Test
    void whenPreparedStatementFailure_throwPreparedStatementCreationFailureException() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select * from hyeon9mak";
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        // when // then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "hyeon9mak best"))
            .isExactlyInstanceOf(PreparedStatementCreationFailureException.class);
    }

    @DisplayName("ResultSet을 못 가져온 경우 QueryExecutionFailureException을 던진다")
    @Test
    void whenResultSetNotCreated_throwQueryExecutionFailureException() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select * from hyeon9mak";
        RowMapper<TestClass> rowMapper = rs -> new TestClass();

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenThrow(new SQLException());

        // when // then
        assertThatThrownBy(() -> jdbcTemplate.query(sql, rowMapper, "hyeon9mak the best"))
            .isExactlyInstanceOf(QueryExecutionFailureException.class);
    }

    private class TestClass {
    }
}
