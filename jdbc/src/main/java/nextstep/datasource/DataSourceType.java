package nextstep.datasource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.function.Function;

public enum DataSourceType {
    H2(properties -> {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(properties.url());
        jdbcDataSource.setUser(properties.user());
        jdbcDataSource.setPassword(properties.password());
        return jdbcDataSource;
    }),
    MYSQL(properties -> {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl(properties.url());
        mysqlDataSource.setUser(properties.user());
        mysqlDataSource.setPassword(properties.password());
        return mysqlDataSource;
    });

    private final Function<DataSourceProperties, DataSource> generator;

    DataSourceType(Function<DataSourceProperties, DataSource> generator) {
        this.generator = generator;
    }

    public DataSource generate(DataSourceProperties dataSourceProperties) {
        return generator.apply(dataSourceProperties);
    }
}
