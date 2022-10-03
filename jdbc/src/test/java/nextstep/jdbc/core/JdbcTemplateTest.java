package nextstep.jdbc.core;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.ResultSetExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void update로_값을_삽입할_수_있다() throws SQLException {
        // given
        final String sql = "insert into test values(?, ?, ?)";

        when(preparedStatement.executeUpdate()).thenReturn(1);

        // when
        final int actual = jdbcTemplate.update(sql, 1, 2, 3);

        // then
        assertAll(
                () -> assertThat(actual).isOne(),
                () -> verify(this.preparedStatement).setInt(1, 1),
                () -> verify(this.preparedStatement).setInt(2, 2),
                () -> verify(this.preparedStatement).setInt(3, 3),
                () -> verify(this.preparedStatement).close(),
                () -> verify(this.connection).close()
        );
    }

    @Test
    void query로_값을_불러올_수_있다() throws SQLException {
        // given
        final String sql = "select id, account from test where id=?";

        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("corinne");

        // when
        final TestUser actual = jdbcTemplate.query(sql, getResultSetExecutor(), 1L);

        // then
        assertAll(
                () -> assertThat(actual).usingRecursiveComparison()
                        .isEqualTo(new TestUser(1L, "corinne")),
                () -> verify(preparedStatement).setLong(1, 1L),
                () -> verify(resultSet).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    void queryList로_값을_불러올_수_있다() throws SQLException {
        // given
        final String sql = "select id, account from test where account=?";

        when(resultSet.next()).thenReturn(false);

        // when
        final List<TestUser> actual = jdbcTemplate.queryForList(sql, getResultSetExecutor(), "corinne");

        // then
        assertAll(
                () -> assertThat(actual).isEmpty(),
                () -> verify(preparedStatement).setString(1, "corinne"),
                () -> verify(resultSet).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    void 커넥션_점유_실패시_예외가_발생한다() throws SQLException {
        // given
        final String sql = "select id, account from test where account=?";
        when(dataSource.getConnection())
                .thenThrow(new DataAccessException());
        final ResultSetExtractor<TestUser> resultSetExecutor = getResultSetExecutor();
        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, resultSetExecutor, "corinne"))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(preparedStatement, times(0)).setString(1, "corinne"),
                () -> verify(preparedStatement, times(0)).close(),
                () -> verify(connection, times(0)).close()
        );
    }

    private ResultSetExtractor<TestUser> getResultSetExecutor() {
        return rs -> {
            final long id = rs.getLong("id");
            final String account = rs.getString("account");
            return new TestUser(id, account);
        };
    }
}
