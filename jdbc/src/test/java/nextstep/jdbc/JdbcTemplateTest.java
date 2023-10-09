package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

class JdbcTemplateTest {

    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
