package nextstep.jdbc;

import nextstep.jdbc.test.DatabasePopulatorUtils;
import nextstep.jdbc.test.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void setup() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        DatabasePopulatorUtils.execute(jdbcDataSource);

        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @DisplayName("입력 기능을 테스트")
    @Test
    void insertTest() {
        //given
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        //when
        jdbcTemplate.insert(sql, pstmt -> {
            pstmt.setString(1, "gugu");
            pstmt.setString(2, "password");
            pstmt.setString(3, "hkkang@woowahan.com");
        });
        //then
    }
}