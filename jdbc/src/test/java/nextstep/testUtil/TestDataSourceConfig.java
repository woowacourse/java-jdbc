package nextstep.testUtil;

import java.util.Objects;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class TestDataSourceConfig {

    private static DataSource INSTANCE;

    public static DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private TestDataSourceConfig() {
    }
}
