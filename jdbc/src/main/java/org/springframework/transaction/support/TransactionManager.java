package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private static final ThreadLocal<Map<DataSource, Boolean>> isTxBegin = ThreadLocal.withInitial(HashMap::new);

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static boolean isTxBegin(final DataSource dataSource) {
        final Boolean flag = isTxBegin.get().get(dataSource);
        if (flag == null) {
            return false;
        }
        return flag;
    }

    public void begin() {
        isTxBegin.get().put(dataSource, true);
    }

    public void commit() {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        try {
            connection.commit();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void rollback() {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        try {
            connection.rollback();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void clear() {
        try {
            final Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
            isTxBegin.get().remove(dataSource);

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
