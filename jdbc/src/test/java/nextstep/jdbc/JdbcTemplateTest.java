package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

class   JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        TestDatabasePopulatorUtils.execute(TestDataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate() {

            @Override
            public DataSource getDataSource() {
                return TestDataSourceConfig.getInstance();
            }
        };
    }

    @Test
    @DisplayName("queryForObject에서 값이 0개면 에러가 발생한다.")
    void queryForObjectZeroResultError() {
        //given
        String sql = "select id, account, password, email from users where id = ?";
        RowMapper rowMapper = (rs) -> rs.getString("test");

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1))
            .isInstanceOf(EmptyResultDataAccessException.class)
            .hasMessage("데이터가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("queryForObject에서 값이 2개 이상이면 에러가 발생한다.")
    void queryForObjectMoreThanTwoResultError() {
        //given
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(insertSql, "runa", "pwd", "email");
        jdbcTemplate.update(insertSql, "runa", "pwd", "email");

        String sql = "select id, account, password, email from users where account = ?";
        RowMapper rowMapper = (rs) -> rs.getString("test");

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "runa"))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class)
            .hasMessage("데이터의 크기가 적절하지 않습니다.");
    }
}