package nextstep.jdbc.app;

import java.util.Objects;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private static DataSource instance;

    public static DataSource getInstance() {
        if (Objects.isNull(instance)) {
            instance = createJdbcDataSource();
        }
        return instance;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();

        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        return jdbcDataSource;
    }

    private DataSourceConfig() {
    }
}
