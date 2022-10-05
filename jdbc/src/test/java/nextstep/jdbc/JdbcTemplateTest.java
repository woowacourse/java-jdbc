package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        this.resultSet = mock(ResultSet.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void queryForObject() throws SQLException {
        final String sql = "select id, account from user where id = ?";
        when(resultSet.next()).thenReturn(true, false);

        final TestObject actual = jdbcTemplate.queryForObject(sql, rs -> new TestObject(1L));

        assertThat(actual.getId()).isEqualTo(1L);
    }

    @Test
    void query() throws SQLException {
        final String sql = "select id, account from user where id = ?";
        when(resultSet.next()).thenReturn(true, false);

        List<TestObject> results = jdbcTemplate.query(sql, rs -> new TestObject(1L), List.of(1L));
        TestObject result = results.get(0);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void update() throws SQLException {
        final String sql = "select id, account from user where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update(sql, new TestObject(1L));

        verify(preparedStatement).executeUpdate();
    }

    private static class TestObject {

        private final Long id;

        public TestObject(final long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
