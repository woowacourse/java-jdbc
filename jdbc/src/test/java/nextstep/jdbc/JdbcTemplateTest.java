package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class JdbcTemplateTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private static final RowMapper<Object> ROW_MAPPER = (rs) -> new Object();

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("커스텀 queryForObject 메서드는 데이터를 조회하여 Optional로 반환")
    @Nested
    class QueryForObjectTest {

        @Test
        void 조회된_로우가_없을_때_빈_Optional_반환() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            Optional<Object> result = jdbcTemplate.queryForObject("select * from table_name where id=1", ROW_MAPPER);

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(1)).next();
            assertThat(result).isEmpty();
        }

        @Test
        void 조회된_로우가_1개_존재하면_해당_데이터가_담긴_Optional_반환() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(false);

            Optional<Object> result = jdbcTemplate.queryForObject("select * from table_name where id=?", ROW_MAPPER, 1);

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(2)).next();
            assertThat(result).isPresent();
        }

        @Test
        void 복수의_로우가_조회된_경우_예외가_발생한다() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            assertThatThrownBy(() -> jdbcTemplate.queryForObject("select * from table_name where id=?", ROW_MAPPER, 1))
                    .isInstanceOf(DataAccessException.class);
        }
    }

    @DisplayName("query 메서드는 데이터를 조회하여 리스트로 반환")
    @Nested
    class QueryTest {

        @Test
        void 조회_대상인_로우가_없을_때_빈_리스트를_반환() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            List<Object> result = jdbcTemplate.query("select * from table_name", ROW_MAPPER);

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(1)).next();
            assertThat(result).hasSize(0);
        }

        @Test
        void 조회_대상인_로우를_전부_담은_리스트를_반환() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

            List<Object> result = jdbcTemplate.query("select * from table_name", ROW_MAPPER);

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(3)).next();
            assertThat(result).hasSize(2);
        }

        @Test
        void 가변인자로_넘긴_매개변수를_받아_PreparedStatement_완성하여_실행() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);
            final var resultSet = mock(ResultSet.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(false);

            String sql = "select * from table_name where id=? and name=?";
            List<Object> result = jdbcTemplate.query(sql, ROW_MAPPER, 1, "name");

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(2)).next();
            assertThat(result).hasSize(1);
        }
    }

    @DisplayName("update 메서드는 데이터를 삽입/수정/삭제하여 변경된 로우의 개수를 반환")
    @Nested
    class UpdateTest {

        @Test
        void 변경한_로우의_개수를_반환() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            int result = jdbcTemplate.update("insert into users (name) values ('name')");

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeUpdate();
            assertThat(result).isOne();
        }

        @Test
        void 가변인자로_넘긴_매개변수를_받아_PreparedStatement_완성하여_실행() throws Exception {
            final var connection = mock(Connection.class);
            final var preparedStatement = mock(PreparedStatement.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(any())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            String sql = "insert into users (name) values (?)";
            int result = jdbcTemplate.update(sql, "name");

            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(any());
            verify(preparedStatement, times(1)).executeUpdate();
            assertThat(result).isOne();
        }
    }
}
