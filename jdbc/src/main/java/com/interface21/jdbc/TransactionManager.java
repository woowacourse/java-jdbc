package com.interface21.jdbc;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import javax.sql.DataSource;

@Component
public class TransactionManager {

    @Inject
    private DataSource dataSource;

    private TransactionManager() {}

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception e) {
            ConnectionConsumerWrapper.accept(connection, Connection::rollback);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            ConnectionConsumerWrapper.accept(connection, releaseConnection());
        }
    }

    private ThrowingConnectionConsumer releaseConnection() {
        return connection -> DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
