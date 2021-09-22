package nextstep.jdbc;

import nextstep.datasource.DataSourceBuilder;
import nextstep.datasource.DataSourceProperties;
import nextstep.datasource.DataSourceType;

import javax.sql.DataSource;

public class JdbcDataSourceBuilder implements DataSourceBuilder {

    private final DataSourceType dataSourceType;
    private final DataSourceProperties dataSourceProperties;

    private JdbcDataSourceBuilder(DataSourceType dataSourceType, DataSourceProperties dataSourceProperties) {
        this.dataSourceType = dataSourceType;
        this.dataSourceProperties = dataSourceProperties;
    }

    public static DataSourceBuilder create(DataSourceType dataSourceType) {
        return new JdbcDataSourceBuilder(dataSourceType, new DataSourceProperties());
    }

    @Override
    public DataSource build() {
        return dataSourceType.generate(dataSourceProperties);
    }
    
    @Override
    public DataSourceBuilder url(String url) {
        dataSourceProperties.url(url);
        return this;
    }

    @Override
    public DataSourceBuilder user(String user) {
        dataSourceProperties.user(user);
        return this;
    }

    @Override
    public DataSourceBuilder password(String password) {
        dataSourceProperties.password(password);
        return this;
    }
}
