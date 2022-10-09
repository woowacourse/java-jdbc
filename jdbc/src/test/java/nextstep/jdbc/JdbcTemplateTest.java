package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String SQL = "";
    private static final RowMapper<String> ROW_MAPPER = rs -> "";

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);

        this.connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        this.preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        this.resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private void verifyAllResourcesClosed() throws Exception {
        final var callTimes = times(1);

        verify(resultSet, callTimes).close();
        verify(preparedStatement, callTimes).close();
        verify(connection, callTimes).close();
    }

    @DisplayName("update 메서드 테스트")
    @Nested
    class UpdateTest {

        private void update() {
            jdbcTemplate.update(SQL, ROW_MAPPER);
        }

        @DisplayName("할당된 모든 자원을 해제한다.")
        @Test
        void closeResources() throws Exception {
            // when
            update();

            // then
            verifyAllResourcesClosed();
        }
    }

    @DisplayName("query 메서드 테스트")
    @Nested
    class QueryTest {

        private void query() {
            jdbcTemplate.query(SQL, ROW_MAPPER);
        }

        @DisplayName("할당된 모든 자원을 해제한다.")
        @Test
        void closeResources() throws Exception {
            // when
            query();

            // then
            verifyAllResourcesClosed();
        }
    }

    @DisplayName("queryForObject 메서드 테스트")
    @Nested
    class QueryForObjectTest {

        private void queryForObject() {
            jdbcTemplate.queryForObject(SQL, ROW_MAPPER);
        }

        @DisplayName("할당된 모든 자원을 해제한다.")
        @Test
        void closeResources() throws Exception {
            // given
            setResultSize(resultSet, 1);

            // when
            queryForObject();

            // then
            verifyAllResourcesClosed();
        }

        @DisplayName("쿼리 실행 결과 값이 존재하지 않으면 예외를 발생시킨다.")
        @Test
        void throw_exception_when_result_size_empty() throws SQLException {
            // given
            setResultSize(resultSet, 0);

            // when & then
            Assertions.assertThatThrownBy(this::queryForObject)
                    .isInstanceOf(EmptyResultDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1, actual " + 0);
        }

        @DisplayName("쿼리 실행 결과 값이 2개 이상이면 예외를 발생시킨다.")
        @Test
        void throw_exception_when_result_size_greater_than_single() throws SQLException {
            // given
            setResultSize(resultSet, 2);

            // when & then
            Assertions.assertThatThrownBy(this::queryForObject)
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1, actual " + 2);
        }

        private void setResultSize(final ResultSet resultSet, final int size) throws SQLException {
            var call = when(resultSet.next());
            for (int i = 0; i < size; i++) {
                call = call.thenReturn(true);
            }
            call.thenReturn(false);
        }
    }
}
