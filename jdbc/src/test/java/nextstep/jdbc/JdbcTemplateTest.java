package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
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
    private AutoCloseable openedMock;

    @BeforeEach
    void setUp() throws SQLException {
        openedMock = MockitoAnnotations.openMocks(this);

        given(dataSource.getConnection())
                .willReturn(connection);
        given(connection.prepareStatement(anyString()))
                .willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void afterAll() throws Exception {
        openedMock.close();
    }

    @Test
    void 데이터_update_쿼리를_실행할_때_update된_레코드_수를_반환한다() throws SQLException {
        // given
        given(preparedStatement.executeUpdate())
                .willReturn(1);

        final String sql = "sql";
        final String arg = "arg";

        // when
        final int affectedRows = jdbcTemplate.update(sql, arg);

        // then
        assertThat(affectedRows).isEqualTo(1);

        assertThatConnectionOpenedButNotClosed();
        assertThatPrepareStatementOpenedAndClosed();
    }

    @Nested
    class 객체_단일_조회_쿼리를_실행할_때 {

        @BeforeEach
        void setUp() throws SQLException {
            given(preparedStatement.executeQuery())
                    .willReturn(resultSet);
        }

        @Test
        void 객체가_한_개_존재하면_객체를_반환한다() throws SQLException {
            // given
            given(resultSet.next())
                    .willReturn(true, false);
            given(rowMapper.mapRow(resultSet))
                    .willReturn(new TestObject());

            final String sql = "sql";
            final String arg = "arg";

            // when
            final TestObject testObject = jdbcTemplate.queryForObject(sql, rowMapper, arg);

            // then
            assertThat(testObject).isNotNull();

            assertThatConnectionOpenedButNotClosed();
            assertThatPrepareStatementOpenedAndClosed();
            assertThatResultSetOpenedAndClosed();
        }

        @Test
        void 객체가_존재하지_않으면_예외를_발생시킨다() throws SQLException {
            // given
            given(resultSet.next())
                    .willReturn(false);

            final String sql = "sql";
            final String arg = "arg";

            // expect
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, arg))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessage("조회 데이터가 존재하지 않습니다.");

            assertThatConnectionOpenedButNotClosed();
            assertThatPrepareStatementOpenedAndClosed();
            assertThatResultSetOpenedAndClosed();
        }

        @Test
        void 객체가_여러_개_존재하면_예외를_발생시킨다() throws SQLException {
            // given
            given(resultSet.next())
                    .willReturn(true, true, false);

            final String sql = "sql";
            final String arg = "arg";

            // expect
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, arg))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessage("조회 데이터가 한 개 이상 존재합니다.");

            assertThatConnectionOpenedButNotClosed();
            assertThatPrepareStatementOpenedAndClosed();
            assertThatResultSetOpenedAndClosed();
        }
    }

    @Nested
    class 객체_다중_조회_쿼리를_실행할_때 {

        @BeforeEach
        void setUp() throws SQLException {
            given(preparedStatement.executeQuery())
                    .willReturn(resultSet);
        }

        @Test
        void 객체가_존재하면_객체_리스트를_반환한다() throws SQLException {
            // given
            given(resultSet.next())
                    .willReturn(true, true, false);
            given(rowMapper.mapRow(resultSet))
                    .willReturn(new TestObject());

            final String sql = "sql";
            final String arg = "arg";

            // when
            List<TestObject> objects = jdbcTemplate.query(sql, rowMapper, arg);

            // then
            assertThat(objects).hasSize(2);

            assertThatConnectionOpenedButNotClosed();
            assertThatPrepareStatementOpenedAndClosed();
            assertThatResultSetOpenedAndClosed();
        }

        @Test
        void 객체가_존재하지_않으면_빈_리스트를_반환한다() throws SQLException {
            // given
            given(resultSet.next())
                    .willReturn(false);

            final String sql = "sql";
            final String arg = "arg";

            // when
            List<TestObject> objects = jdbcTemplate.query(sql, rowMapper, arg);

            // then
            assertThat(objects).isEmpty();

            assertThatConnectionOpenedButNotClosed();
            assertThatPrepareStatementOpenedAndClosed();
            assertThatResultSetOpenedAndClosed();
        }
    }

    private void assertThatConnectionOpenedButNotClosed() throws SQLException {
        then(dataSource)
                .should(times(1))
                .getConnection();

        then(connection)
                .should(never())
                .close();
    }

    private void assertThatPrepareStatementOpenedAndClosed() throws SQLException {
        then(connection)
                .should(times(1))
                .prepareStatement(anyString());

        then(preparedStatement)
                .should(times(1))
                .close();
    }

    private void assertThatResultSetOpenedAndClosed() throws SQLException {
        then(preparedStatement)
                .should(times(1))
                .executeQuery();

        then(resultSet)
                .should(times(1))
                .close();
    }

    private static class TestObject {
    }
}
