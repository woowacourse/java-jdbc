package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcTemplateTransactionManager implements TransactionManager {

    private TransactionSynchronizationManager synchronizationManager;

    public JdbcTemplateTransactionManager(final TransactionSynchronizationManager synchronizationManager) {
        this.synchronizationManager = synchronizationManager;
    }

    @Override
    public void getTransaction(final DataSource dataSource){
        Connection connection = null;
        try {
            synchronizationManager.clear();
            connection = dataSource.getConnection();
            synchronizationManager.set(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {
        final Connection connection = synchronizationManager.get();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        synchronizationManager.clear();
    }

    @Override
    public void rollback() {
        final Connection connection = synchronizationManager.get();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        synchronizationManager.clear();
    }
}
