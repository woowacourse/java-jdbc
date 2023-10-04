package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.TestDataSourceConfig;
import org.springframework.jdbc.core.JdbcTemplateException.MoreDataAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.NoDataAccessException;

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

        TestMember testMember = jdbcTemplate.find("select id, name from member where name = '콩하나';",
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(testMember.getName()).isEqualTo(expectedName);
    }

    @Test
    void sql문을_통해_하나의_객체를_조회할_때_여러_객체가_존재하면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> jdbcTemplate.find("select id, name from member;",
                        (rs) -> new TestMember(
                                rs.getLong("id"),
                                rs.getString("name")
                        )))
                .isInstanceOf(MoreDataAccessException.class);
    }

    @Test
    void sql문을_통해_하나의_객체를_조회할_때_객체가_존재하지_않으면_예외가_발생한다() {
        Assertions.assertThatThrownBy(
                        () -> jdbcTemplate.find("select id, name from member where id = " + Long.MAX_VALUE + ";",
                                (rs) -> new TestMember(
                                        rs.getLong("id"),
                                        rs.getString("name")
                                )))
                .isInstanceOf(NoDataAccessException.class);
    }

    @Test
    void sql문을_통해_여러_객체를_조회할_수_있다() {
        String expectedName1 = "콩하나";
        String expectedName2 = "콩둘";

        List<TestMember> testMembers = jdbcTemplate.findAll("select id, name from member;",
                (rs) -> new TestMember(
                        rs.getLong("id"),
                        rs.getString("name")
                ));

        assertThat(testMembers).map(TestMember::getName).containsExactlyInAnyOrder(expectedName1, expectedName2);
    }
}
