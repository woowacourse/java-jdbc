package nextstep.jdbc;

import java.util.function.Supplier;
import javax.sql.DataSource;

public class TransactionTemplateManager {

    private final TransactionManager txManager;
    private final TransactionSynchronizationManager syncManager;
    private final DataSource dataSource;

    public TransactionTemplateManager(final TransactionManager txManager,
                                      final TransactionSynchronizationManager syncManager,
                                      final DataSource dataSource) {
        this.txManager = txManager;
        this.syncManager = syncManager;
        this.dataSource = dataSource;
    }

    public <T> T doTransaction(final Supplier<T> supplier) {
        txManager.getTransaction(dataSource);
        T result = null;
        try {
            result = supplier.get();
            txManager.commit();
        } catch (Exception ex) {
            txManager.rollback();
            throw new DataAccessException(ex);
        }
        return result;
    }

    public void doTransaction(final Voider voider) {
        txManager.getTransaction(dataSource);
        try {
            voider.execute();
            txManager.commit();
        } catch (Exception ex) {
            txManager.rollback();
            throw new DataAccessException(ex);
        }
    }
}
