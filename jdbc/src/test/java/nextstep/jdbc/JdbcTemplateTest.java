package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {
    private static final RowMapper<TestUser> ROW_MAPPER = (resultSet -> new TestUser("test_email", "test_password"));

    private final DataSource dataSource = mock(DataSource.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "insert into test_user (email, password) values (?, ?, ?)";

        // when
        jdbcTemplate.update(sql, "test_email", "test_password");

        // then
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        final String sql = "select email, password from test_user where email = ?";
        when(resultSet.next()).thenReturn(true);

        // when
        final TestUser testUser = jdbcTemplate.queryForObject(sql, ROW_MAPPER, "test_email").get();

        // then
        assertThat(testUser).isEqualTo(new TestUser("test_email", "test_password"));
    }

    @Test
    void query() throws SQLException {
        // given
        final var sql = "select * from test_user";
        when(resultSet.next()).thenReturn(true, true, false);

        // when
        final List<TestUser> testUsers = jdbcTemplate.queryForList(sql, ROW_MAPPER);

        // then
        assertSoftly(softly -> {
            softly.assertThat(testUsers).hasSize(2);
            softly.assertThat(testUsers.get(0)).isEqualTo(new TestUser("test_email", "test_password"));
            softly.assertThat(testUsers.get(1)).isEqualTo(new TestUser("test_email", "test_password"));
        });
    }
}
