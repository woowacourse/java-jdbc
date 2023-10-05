package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class JdbcTemplateTest {

    private final Connection connection = mock(Connection.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection())
                .thenReturn(connection);
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
    }

    @Nested
    class update는 {

        @Test
        void 쿼리문을_실행한다() throws Exception {
            // given
            final String sql = "update users set name = ? where nickname = ?";

            // when
            jdbcTemplate.update(sql, "홍고", "hongo");

            // then
            verify(preparedStatement, times(1)).executeUpdate();
        }

        @Test
        void 커넥션을_연결한다() throws Exception {
            // given
            final String sql = "update users set name = ? where nickname = ?";

            // when
            jdbcTemplate.update(sql, "홍고", "hongo");

            // then
            verify(connection, times(1)).prepareStatement(sql);
        }

    }

    @Nested
    class queryForObject는 {

        @Test
        void 단건_결과를_조회할_수_있다() throws Exception {
            // given
            when(resultSet.next()).thenReturn(true, false);
            final String sql = "select * from users where id = ?";

            // when
            Optional<Object> result = jdbcTemplate.queryForObject(resultSet -> new Object(), sql, "hongo");

            // then
            assertThat(result).isNotEmpty();
        }


        @Test
        void 결과가_존재하지_않으면_Optional을_반환한다() throws Exception {
            // given
            when(resultSet.next()).thenReturn(false);
            final String sql = "select * from users where id = ?";

            // when
            Optional<Object> result = jdbcTemplate.queryForObject(resultSet -> new Object(), sql, "hongo");

            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    class query는 {

        @Test
        void 여러건의_결과를_조회할_수_있다() throws Exception {
            // given
            when(resultSet.next()).thenReturn(true, true, true, false);
            final String sql = "select * from users where gender = ?";

            // when
            List<Object> results = jdbcTemplate.query(resultSet -> new Object(), sql, "male");

            // then
            assertThat(results).isNotEmpty();
        }

        @Test
        void 결과가_존재하지_않으면_빈_리스트을_반환한다() throws Exception {
            // given
            when(resultSet.next()).thenReturn(false);
            final String sql = "select * from users where gender = ?";

            // when
            List<Object> results = jdbcTemplate.query(resultSet -> new Object(), sql, "male");

            // then
            assertThat(results).isEmpty();
        }

    }

}
