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

    public void start(boolean readOnly) {
        transactionTemplate.execute(connection -> {
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
        });
    }

    public void commit() {
        transactionTemplate.execute(Connection::commit);
        close();
    }

    public void close() {
        transactionTemplate.execute(connection -> DataSourceUtils.releaseConnection(connection, dataSource));
    }

    public void rollback() {
        transactionTemplate.execute(Connection::rollback);
        close();
    }

}
