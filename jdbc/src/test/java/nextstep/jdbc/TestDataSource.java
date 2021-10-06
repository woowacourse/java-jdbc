package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public class TestDataSource {

    private static DataSource dataSource;

    public static DataSource getInstance() {
        if (Objects.isNull(dataSource)) {
            dataSource = createJdbcDataSource();
        }
        return dataSource;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}
