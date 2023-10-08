package org.springframework.transaction;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final TransactionTemplate transactionTemplate;
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public void start() {
        transactionTemplate.execute(connection -> connection.setAutoCommit(false));
    }

    public void commit() {
        transactionTemplate.execute(Connection::commit);
        close();
    }

    private void close() {
        transactionTemplate.execute(connection -> {
            connection.close();
            DataSourceUtils.releaseConnection(connection, dataSource);
        });
    }

    public void rollback() {
        transactionTemplate.execute(Connection::rollback);
        close();
    }

}
