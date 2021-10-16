package nextstep.datasource;

import javax.sql.DataSource;

public interface DataSourceBuilder {

    DataSource build();

    DataSourceBuilder url(String url);

    DataSourceBuilder user(String user);

    DataSourceBuilder password(String password);
}
