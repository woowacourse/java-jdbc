package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public class TestDataSourceConfig {
    private static javax.sql.DataSource INSTANCE = createJdbcDataSource();

    private TestDataSourceConfig() {
    }

    public static javax.sql.DataSource getInstance() {
        return INSTANCE;
    }

    public static void truncate() {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(false, false, "UTF-8",
            new ClassPathResource("truncate.sql"));
        resourceDatabasePopulator.execute(INSTANCE);
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(false, false, "UTF-8",
            new ClassPathResource("schema.sql"));
        resourceDatabasePopulator.execute(jdbcDataSource);

        return jdbcDataSource;
    }
}
