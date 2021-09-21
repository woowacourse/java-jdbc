package nextstep.config;

import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private static final javax.sql.DataSource INSTANCE = createJdbcDataSource();

    private DataSourceConfig() {}

    public static javax.sql.DataSource getInstance() {
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
