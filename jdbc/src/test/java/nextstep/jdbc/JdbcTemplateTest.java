package nextstep.jdbc;

import nextstep.jdbc.config.TestDataSourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    private Member member;

    private RowMapper<Member> rowMapper = rs -> {
        Long id = rs.getLong("id");
        String nickname = rs.getString("nickname");
        return new Member(id, nickname);
    };

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());
        member = new Member(1L, "test");

        String sql = "insert into members (id, nickname) values(?, ?)";
        jdbcTemplate.update(sql, member.getId(), member.getNickname());
    }

    @AfterEach
    void tearDown() {
        String sql = "delete from members";
        jdbcTemplate.update(sql);
    }

    @Test
    void queryForObjectTest() {
        // given
        String sql = "select * from members where id = ?";

        // when
        Member result = jdbcTemplate.queryForObject(sql, rowMapper, member.getId());

        // then
        assertThat(result).isEqualTo(member);
    }

    @Test
    void queryTest() {
        // given
        String sql = "select * from members";

        // when
        List<Member> result = jdbcTemplate.query(sql, rowMapper);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void updateTest() {
        // given
        String sql = "update members set nickname = ? where id = ?";
        String expected = "newMember";

        // when
        jdbcTemplate.update(sql, expected, member.getId());

        // then
        String select = "select * from members where id = ?";
        Member found = jdbcTemplate.queryForObject(select, rowMapper, member.getId());

        assertThat(found.getNickname()).isEqualTo(expected);
    }

    class Member {
        private Long id;
        private String nickname;

        public Member(Long id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }

        public Long getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Member member = (Member) o;
            return Objects.equals(id, member.id) && Objects.equals(nickname, member.nickname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nickname);
        }
    }
}
