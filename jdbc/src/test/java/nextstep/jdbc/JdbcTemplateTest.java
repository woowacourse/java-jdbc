package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTest {

    static class Member {
        private final Long id;
        private final String username;

        public Member(final Long id, final String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }

    private final String sql = "SELECT * FROM MEMBER WHERE username = ?";
    private final RowMapper<Member> memberMapper = (resultSet) -> new Member(
            resultSet.getLong(1),
            resultSet.getString(2)
    );
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void 단일_조회시_쿼리결과가_있을_경우_해당_객체를_반환한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getString(2)).thenReturn("blackcat");

        // when
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final Optional<Member> found = jdbcTemplate.queryForObject(sql, memberMapper, "blackcat");

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(found).isNotEmpty();
            softly.assertThat(found.get().getId()).isEqualTo(1L);
            softly.assertThat(found.get().getUsername()).isEqualTo("blackcat");
        });
    }

    @Test
    void 단일_조회시_쿼리결과가_없을_경우_빈_옵셔널을_반환한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(false);

        // when
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final Optional<Member> found = jdbcTemplate.queryForObject(sql, memberMapper, "blackcat");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 복수_조회시_쿼리_결과가_없을_경우_빈_리스트를_반환한다() throws SQLException {
        // given
        when(resultSet.next()).thenReturn(false);

        // when
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Member> query = jdbcTemplate.query(sql, memberMapper, "blackcat");

        // then
        assertThat(query).isEmpty();
    }
}
