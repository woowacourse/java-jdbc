package nextstep.jdbc.support;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class DataSourceConfig {

    private static final DataSource INSTANCE = createJdbcDataSource();

    private DataSourceConfig() {
    }

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
