package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.function.Supplier;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserTxService implements UserService {

    private final UserService target;

    @Override
    public User findById(final long id) {
        return target.findById(id);
    }

    @Override
    public void save(final User user) {
        target.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        try {
            executeInTransaction(() -> target.changePassword(id, newPassword, createdBy));
        } catch (final Exception e) {
            throw new RuntimeException("비밀번호 변경 실패", e);
        }
    }

    // TODO 분리 가능, 반환값(Supplier)
    private void executeInTransaction(final Runnable action) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        try (final Connection connection = DataSourceUtils.getConnection(dataSource)) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                action.run();
                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }

        } catch (final Exception e) {
            throw new RuntimeException("트랜잭션 실행 실패", e);
        }
    }
}
