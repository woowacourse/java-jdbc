package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public interface TransactionManager {

    static Connection getConnection(DataSource dataSource) {
        return DataSourceUtils.getConnection(dataSource);
    }

    void execute(DataSource dataSource, TransactionExecutor method);

    <T> T executeAndReturn(DataSource dataSource, TransactionSupplier<T> method);
}
