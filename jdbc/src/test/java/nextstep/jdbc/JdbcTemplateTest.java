package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private RowMapper<TestObject> rowMapper;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        given(dataSource.getConnection())
                .willReturn(connection);
        given(connection.prepareStatement(anyString()))
                .willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        // given
        given(preparedStatement.executeUpdate())
                .willReturn(1);

        final String sql = "sql";
        final String arg = "arg";

        // when
        final int actual = jdbcTemplate.update(sql, arg);

        // then
        assertThat(actual).isEqualTo(1);

        then(dataSource)
                .should(times(1))
                .getConnection();
        then(connection)
                .should(times(1))
                .prepareStatement(anyString());

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        then(connection)
                .should(times(1))
                .close();
        then(preparedStatement)
                .should(times(1))
                .close();
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        given(rowMapper.mapRow(resultSet))
                .willReturn(new TestObject());
        given(preparedStatement.executeQuery())
                .willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true);

        final String sql = "sql";
        final String arg = "arg";

        // when
        final TestObject testObject = jdbcTemplate.queryForObject(sql, rowMapper, arg);

        // then
        assertThat(testObject).isNotNull();

        then(dataSource)
                .should(times(1))
                .getConnection();
        then(connection)
                .should(times(1))
                .prepareStatement(anyString());
        then(preparedStatement)
                .should(times(1))
                .executeQuery();

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        then(connection)
                .should(times(1))
                .close();
        then(preparedStatement)
                .should(times(1))
                .close();
        then(resultSet)
                .should(times(1))
                .close();
    }

    @Test
    void queryForObject_null() throws SQLException {
        // given
        given(preparedStatement.executeQuery())
                .willReturn(resultSet);
        given(resultSet.next())
                .willReturn(false);

        final String sql = "sql";
        final String arg = "arg";

        // when
        final TestObject testObject = jdbcTemplate.queryForObject(sql, rowMapper, arg);

        // then
        assertThat(testObject).isNull();

        then(dataSource)
                .should(times(1))
                .getConnection();
        then(connection)
                .should(times(1))
                .prepareStatement(anyString());
        then(preparedStatement)
                .should(times(1))
                .executeQuery();

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        then(connection)
                .should(times(1))
                .close();
        then(preparedStatement)
                .should(times(1))
                .close();
    }

    private static class TestObject {
    }
}
