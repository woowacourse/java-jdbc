package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import nextstep.jdbc.testUtil.TestDataSourceConfig;
import nextstep.jdbc.testUtil.TestDatabaseUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.exception.IncorrectResultSizeDataAccessException;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTest {

    private static final JdbcTemplate jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());
    ;

    private static final class Member {
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

    private final String sql = "SELECT * FROM member WHERE username = ?";
    private final RowMapper<Member> memberMapper = (resultSet) -> new Member(
            resultSet.getLong(1),
            resultSet.getString(2)
    );

    @BeforeAll
    static void setUp() {
        TestDatabaseUtils.execute(TestDataSourceConfig.getInstance());
    }

    @Test
    void 단일_조회시_단일_쿼리결과가_있을_경우_해당_객체를_반환한다() {
        // given
        final String username = "blackcat";

        // when
        final Optional<Member> found = jdbcTemplate.queryForObject(sql, memberMapper, username);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(found).isNotEmpty();
            softly.assertThat(found.get().getId()).isEqualTo(1L);
            softly.assertThat(found.get().getUsername()).isEqualTo("blackcat");
        });
    }

    @Test
    void 단일_조회시_쿼리결과가_없을_경우_빈_옵셔널을_반환한다() {
        // given
        final String username = "blackcat";

        // when
        final Optional<Member> found = jdbcTemplate.queryForObject(sql, memberMapper, username + Long.MAX_VALUE);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 단일_조회시_쿼리결과가_여러_개일_경우_예외가_발생한다() {
        // given
        final String username = "gugu";

        // expected
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, memberMapper, username))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void 복수_조회시_쿼리_결과가_없을_경우_빈_리스트를_반환한다() {
        // given
        final String username = "blackcat";

        // when
        List<Member> query = jdbcTemplate.query(sql, memberMapper, username + Long.MAX_VALUE);

        // then
        assertThat(query).isEmpty();
    }
}
