package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.TestDataSourceConfig;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());

    @Test
    void sql문을_실행할_수_있다() {
        // 생성
        jdbcTemplate.execute("insert into member (name) values ('콩하나 인서트');");

        try (Connection conn = TestDataSourceConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("select id from member where name = '콩하나 인서트';")) {

            assertThat(ps.executeQuery().next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 삭제
        jdbcTemplate.execute("delete from member where name = ('콩하나 인서트');");
        try (Connection conn = TestDataSourceConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("select id from member where name = '콩하나 인서트';")) {

            assertThat(ps.executeQuery().next()).isFalse();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sql문을_통해_객체를_조회할_수_있다() {
        String expectedName = "콩하나";

        Member member = jdbcTemplate.find("select id, name from member where name = '콩하나';",
                (rs) -> new Member(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(member.getName()).isEqualTo(expectedName);
    }


    @Test
    void sql문을_통해_여러_객체를_조회할_수_있다() {
        String expectedName1 = "콩하나";
        String expectedName2 = "콩둘";

        List<Member> members = jdbcTemplate.findAll("select id, name from member;",
                (rs) -> new Member(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(members).map(Member::getName).containsExactlyInAnyOrder(expectedName1, expectedName2);
    }
}
