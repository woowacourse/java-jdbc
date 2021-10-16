package nextstep.datasource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.function.Function;

public enum DataSourceType {
    H2(properties -> {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(properties.getUrl());
        jdbcDataSource.setUser(properties.getUser());
        jdbcDataSource.setPassword(properties.getPassword());
        return jdbcDataSource;
    }),
    MYSQL(properties -> {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl(properties.getUrl());
        mysqlDataSource.setUser(properties.getUser());
        mysqlDataSource.setPassword(properties.getPassword());
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
