package nextstep.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public class TestJdbcTemplate extends JdbcTemplate {

    public TestJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected DataSource getDataSource() {
        return null;
    }
}
