package nextstep.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

class JdbcTemplateTest {
    private JdbcTemplate jdbcTemplate;

    @Test
    void insert() {
        DataSource dataSource = mock(DataSource.class);
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(sql, "sally", "password", "sally@hi");


    }
}
