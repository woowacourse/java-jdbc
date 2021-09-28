package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.SQLException;
import java.util.Objects;

public class DataSourceTestConfig {
    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() throws SQLException {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
