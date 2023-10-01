package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private static final RowMapper<Member> MEMBER_MAPPER = rs -> {
        long id = rs.getLong(1);
        String name = rs.getString(2);
        return new Member(id, name);
    };

    JdbcTemplate jdbcTemplate;
    DataSource dataSource;
    Connection conn;
    PreparedStatement pstmt;
    ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection())
            .thenReturn(conn);
        when(conn.prepareStatement(anyString()))
            .thenReturn(pstmt);
        when(pstmt.executeQuery())
            .thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObject_withParameters_success() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        Member expect = new Member(1L, "glen");
        when(rs.next())
            .thenReturn(true, false);
        when(rs.getLong(1))
            .thenReturn(expect.getId());
        when(rs.getString(2))
            .thenReturn(expect.getName());

        // when
        Member actual = jdbcTemplate.queryForObject(sql, MEMBER_MAPPER, 1);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expect);
    }

    @Test
    void queryForObject_withParameters_exception_with_emptyResult() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        when(rs.next())
            .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, MEMBER_MAPPER, 1))
            .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject_withParameters_exception_with_incorrectResultSize() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        when(rs.next())
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, MEMBER_MAPPER, 1))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void queryForObject_withPstmtSetter_success() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        Member expect = new Member(1L, "glen");
        when(rs.next())
            .thenReturn(true, false);
        when(rs.getLong(1))
            .thenReturn(expect.getId());
        when(rs.getString(2))
            .thenReturn(expect.getName());

        // when
        Member actual = jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setLong(1, expect.getId()), MEMBER_MAPPER);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expect);
    }

    @Test
    void queryForObject_withPstmtSetter_exception_with_emptyResult() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        when(rs.next())
            .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> {
            jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setLong(1, 1L), MEMBER_MAPPER);
        }).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void queryForObject_withPstmtSetter_exception_with_incorrectResultSize() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        when(rs.next())
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setLong(1, 1L), MEMBER_MAPPER);
        }).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void query_success() throws Exception {
        // given
        String sql = "select id, name from member";
        List<Member> expect = List.of(new Member(1L, "glen"), new Member(2L, "fiddich"));
        when(rs.next())
            .thenReturn(true, true, false);
        when(rs.getLong(1))
            .thenReturn(expect.get(0).getId(), expect.get(1).getId());
        when(rs.getString(2))
            .thenReturn(expect.get(0).getName(), expect.get(1).getName());

        // when
        List<Member> actual = jdbcTemplate.query(sql, MEMBER_MAPPER);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expect);
    }

    @Test
    void query_emptyRow_success() throws Exception {
        // given
        String sql = "select id, name from member";
        when(rs.next())
            .thenReturn(false);

        // when
        List<Member> actual = jdbcTemplate.query(sql, MEMBER_MAPPER);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void query_pstmtSetter_success() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        List<Member> expect = List.of(new Member(1L, "glen"));
        when(rs.next())
            .thenReturn(true, false);
        when(rs.getLong(1))
            .thenReturn(expect.get(0).getId());
        when(rs.getString(2))
            .thenReturn(expect.get(0).getName());

        // when
        List<Member> actual = jdbcTemplate.query(sql, ps -> ps.setLong(1, 1L), MEMBER_MAPPER);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expect);
    }

    @Test
    void query_parameters_success() throws Exception {
        // given
        String sql = "select id, name from member where id = ?";
        List<Member> expect = List.of(new Member(1L, "glen"));
        when(rs.next())
            .thenReturn(true, false);
        when(rs.getLong(1))
            .thenReturn(expect.get(0).getId());
        when(rs.getString(2))
            .thenReturn(expect.get(0).getName());

        // when
        List<Member> actual = jdbcTemplate.query(sql, MEMBER_MAPPER, 1L);

        // then
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expect);
    }

    static class Member {

        private final Long id;
        private final String name;

        public Member(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
