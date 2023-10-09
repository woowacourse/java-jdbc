package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.jdbc.core.test_supporter.DataSourceConfig.getInstance;
import static org.springframework.transaction.support.TransactionSynchronizationManager.isTransactionActive;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("datasource를 key값으로 connection을 반환한다.")
    void getConnection() throws SQLException {
        final DataSource ds = getInstance();
        final Connection connection = ds.getConnection();
        TransactionSynchronizationManager.bindResource(ds, connection);

        final Connection foundConnection = TransactionSynchronizationManager.getResource(ds);

        assertThat(foundConnection)
            .isEqualTo(connection);
    }


    @Test
    @DisplayName("datasource를 key값으로 connection을 저장한다.")
    void bindResource() throws SQLException {
        final DataSource ds = getInstance();
        final Connection connection = ds.getConnection();

        TransactionSynchronizationManager.bindResource(ds, connection);

        final Connection foundConnection = TransactionSynchronizationManager.getResource(ds);
        assertThat(foundConnection)
            .isEqualTo(connection);
    }

    @Test
    @DisplayName("key값에 해당하는 tuple을 제거한다.")
    void unbindResource() throws SQLException {
        final DataSource ds = getInstance();
        final Connection connection = ds.getConnection();
        TransactionSynchronizationManager.bindResource(ds, connection);

        TransactionSynchronizationManager.unbindResource(ds);

        final Connection newConnection = TransactionSynchronizationManager.getResource(ds);
        assertThat(newConnection)
            .isNotEqualTo(connection);
    }

    @Nested
    @DisplayName("트랜잭션이 실행됬는지에 대한 여부를 반환한다.")
    class IsTransactionActive {

        @Test
        @DisplayName("실행한 경우")
        void trueCase() throws SQLException {
            final DataSource datasource = getInstance();
            TransactionSynchronizationManager.bindResource(datasource, datasource.getConnection());

            assertTrue(isTransactionActive(getInstance()));
        }

        @Test
        @DisplayName("실행하지 않은 경우")
        void falseCase() throws SQLException {
            assertFalse(isTransactionActive(getInstance()));
        }
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }
}
