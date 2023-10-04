package nextstep.jdbc.config;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class TestDataSourceConfig {

    private static DataSource INSTANCE;

    public static DataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static DataSource createJdbcDataSource() {
        final var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
