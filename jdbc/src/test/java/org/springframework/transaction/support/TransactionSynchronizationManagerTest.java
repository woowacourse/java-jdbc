package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("datasource를 key값으로 connection을 반환한다.")
    void getConnection() throws SQLException {
        final DataSource ds = DataSourceConfig.getInstance();
        final Connection connection = ds.getConnection();
        TransactionSynchronizationManager.bindResource(ds, connection);

        final Connection foundConnection = TransactionSynchronizationManager.getResource(ds);

        assertThat(foundConnection)
            .isEqualTo(connection);
    }


    @Test
    @DisplayName("datasource를 key값으로 connection을 저장한다.")
    void bindResource() throws SQLException {
        final DataSource ds = DataSourceConfig.getInstance();
        final Connection connection = ds.getConnection();

        TransactionSynchronizationManager.bindResource(ds, connection);

        final Connection foundConnection = TransactionSynchronizationManager.getResource(ds);
        assertThat(foundConnection)
            .isEqualTo(connection);
    }

    @Test
    @DisplayName("key값에 해당하는 tuple을 제거한다.")
    void unbindResource() throws SQLException {
        final DataSource ds = DataSourceConfig.getInstance();
        final Connection connection = ds.getConnection();
        TransactionSynchronizationManager.bindResource(ds, connection);

        TransactionSynchronizationManager.unbindResource(ds);

        final Connection newConnection = TransactionSynchronizationManager.getResource(ds);
        assertThat(newConnection)
            .isNotEqualTo(connection);
    }
}
