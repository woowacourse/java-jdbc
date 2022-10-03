package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import nextstep.jdbc.support.DataSourceConfig;
import nextstep.jdbc.support.DatabasePopulatorUtils;
import nextstep.jdbc.support.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String TRUNCATE_USER_TABLE = "TRUNCATE TABLE users RESTART IDENTITY";

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(TRUNCATE_USER_TABLE);
    }

    @Nested
    @DisplayName("queryForObject 메서드는")
    class QueryForObject {

        @Test
        @DisplayName("일치하는 레코드를 조회 후 RowMapper로 객체를 생성해서 반환한다.")
        void success() {
            // given
            final String insertSql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
            final String account = "rick";

            jdbcTemplate.update(insertSql, account, "rick123", "admin@levellog.app");

            final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";

            // when
            final User actual = jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, account);

            // then
            assertThat(actual).isNotNull();
        }

        @Test
        @DisplayName("일치하는 레코드가 한 건 이상이면 예외를 던진다.")
        void queryForObject_moreThenOne_exception() {
            // given
            final String insertSql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
            final String email = "admin@levellog.app";

            jdbcTemplate.update(insertSql, "rick", "rick123", email);
            jdbcTemplate.update(insertSql, "roma", "roma123", email);

            final String sql = "SELECT id, account, password, email FROM users WHERE email = ?";

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, email))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1 but 2");
        }

        @Test
        @DisplayName("일치하는 레코드가 존재하지 않으면 예외를 던진다.")
        void queryForObject_recordNotExist_exception() {
            // given
            final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
            final Long userId = 999L;

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, userId))
                    .isInstanceOf(EmptyResultDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1 but 0");
        }
    }
}
