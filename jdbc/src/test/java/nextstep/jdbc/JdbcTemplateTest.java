package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
    void 데이터를_update하는_쿼리를_실행하고_update된_레코드_수를_반환한다() throws SQLException {
        // given
        given(preparedStatement.executeUpdate())
                .willReturn(1);

        final String sql = "sql";
        final String arg = "arg";

        // when
        final int actual = jdbcTemplate.update(sql, arg);

        // then
        assertThat(actual).isEqualTo(1);

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        assertThatConnectionHasOpenedAndClosed();
        assertThatPrepareStatementHasOpenedAndClosed();
    }

    private void assertThatConnectionHasOpenedAndClosed() throws SQLException {
        then(dataSource)
                .should(times(1))
                .getConnection();

        then(connection)
                .should(times(1))
                .close();
    }

    private void assertThatPrepareStatementHasOpenedAndClosed() throws SQLException {
        then(connection)
                .should(times(1))
                .prepareStatement(anyString());

        then(preparedStatement)
                .should(times(1))
                .close();
    }

    @Test
    void 객체_단일_조회_쿼리를_실행할_때_객체가_존재하면_객체를_반환한다() throws SQLException {
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

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        assertThatConnectionHasOpenedAndClosed();
        assertThatPrepareStatementHasOpenedAndClosed();
        assertThatResultSetHasOpenedAndClosed();
    }

    private void assertThatResultSetHasOpenedAndClosed() throws SQLException {
        then(preparedStatement)
                .should(times(1))
                .executeQuery();

        then(resultSet)
                .should(times(1))
                .close();
    }

    @Test
    void 객체_단일_조회_쿼리를_실행할_때_객체가_존재하지_않으면_null을_반환한다() throws SQLException {
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

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        assertThatConnectionHasOpenedAndClosed();
        assertThatPrepareStatementHasOpenedAndClosed();
        assertThatResultSetHasOpenedAndClosed();
    }

    @Test
    void 객체_다중_조회_쿼리를_실행할_때_객체가_존재하면_객체_리스트를_반환한다() throws SQLException {
        // given
        given(preparedStatement.executeQuery())
                .willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true, true, false);

        final String sql = "sql";
        final String arg = "arg";

        // when
        List<TestObject> objects = jdbcTemplate.query(sql, rowMapper, arg);

        // then
        assertThat(objects).hasSize(2);

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        assertThatConnectionHasOpenedAndClosed();
        assertThatPrepareStatementHasOpenedAndClosed();
        assertThatResultSetHasOpenedAndClosed();
    }

    @Test
    void 객체_다중_조회_쿼리를_실행할_때_객체가_존재하지_않으면_빈_리스트를_반환한다() throws SQLException {
        // given
        given(preparedStatement.executeQuery())
                .willReturn(resultSet);
        given(resultSet.next())
                .willReturn(false);

        final String sql = "sql";
        final String arg = "arg";

        // when
        List<TestObject> objects = jdbcTemplate.query(sql, rowMapper, arg);

        // then
        assertThat(objects).isEmpty();

        then(preparedStatement)
                .should(times(1))
                .setObject(1, "arg");

        assertThatConnectionHasOpenedAndClosed();
        assertThatPrepareStatementHasOpenedAndClosed();
        assertThatResultSetHasOpenedAndClosed();
    }

    private static class TestObject {
    }
}
