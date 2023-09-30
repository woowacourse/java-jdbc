package org.springframework.jdbc.support;

import javax.sql.DataSource;

public abstract class JdbcAccessor {

    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
