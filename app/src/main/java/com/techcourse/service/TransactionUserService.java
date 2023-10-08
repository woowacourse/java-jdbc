package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource dataSource;


    public TransactionUserService(final AppUserService appUserService, final DataSource dataSource) {
        this.appUserService = appUserService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return new TransactionExecutor()
                .setDataSource(dataSource)
                .setReadOnlyTrue()
                .executeTransactionWithSupplier(() -> appUserService.findById(id));
    }

    @Override
    public void insert(final User user) {
        new TransactionExecutor()
                .setDataSource(dataSource)
                .executeTransactionWithRunnable(() -> appUserService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        new TransactionExecutor()
                .setDataSource(dataSource)
                .executeTransactionWithRunnable(() -> appUserService.changePassword(id, newPassword, createBy));
    }

    private static class TransactionExecutor {

        private DataSource dataSource;
        private boolean readOnly = false;

        private TransactionExecutor setReadOnlyTrue() {
            this.readOnly = true;
            return this;
        }

        private TransactionExecutor setDataSource(final DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        private void executeTransactionWithRunnable(final Runnable runnable) {
            executeTransactionWithSupplier(() -> {
                runnable.run();
                return null;
            });
        }

        private <T> T executeTransactionWithSupplier(Supplier<T> supplier) {
            Transaction transaction = Transaction.start(dataSource);
            transaction.setReadOnly(readOnly);
            try {
                T result = supplier.get();
                transaction.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                transaction.rollback();
                throw new DataAccessException(e);
            } finally {
                transaction.close();
            }
        }
    }
}
