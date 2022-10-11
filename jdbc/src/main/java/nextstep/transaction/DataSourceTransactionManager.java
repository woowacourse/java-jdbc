package nextstep.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

public class DataSourceTransactionManager implements PlatformTransactionManager {

    private final DataSource dataSource;
//    private static Connection connection;

    public DataSourceTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TransactionStatus getTransaction(final TransactionDefinition definition) throws TransactionException {
        TransactionStatus transactionStatus = null;
        boolean isNewTransaction = false;
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            isNewTransaction = true;
            transactionStatus = new DefaultTransactionStatus(connection, isNewTransaction, isNewTransaction, definition.isReadOnly(), false, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionStatus;

    }

    @Override
    public void commit(final TransactionStatus status) throws TransactionException {
        try {
            DefaultTransactionStatus transactionStatus = (DefaultTransactionStatus) status;
            Connection connection = (Connection) transactionStatus.getTransaction();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback(final TransactionStatus status) throws TransactionException {
        try {
            DefaultTransactionStatus transactionStatus = (DefaultTransactionStatus) status;
            Connection connection = (Connection) transactionStatus.getTransaction();
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
