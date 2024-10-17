package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;

    public TransactionSynchronizationManagerTest() {
        this.dataSource = mock(DataSource.class);
    }

    @Test
    @DisplayName("저장된 Resource가 없는 경우 null을 반환한다.")
    void getResource_WhenNotStored() {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        assertNull(connection);
    }

    @Test
    @DisplayName("Resource가 저장되어 있는 경우 저장된 Connection을 불러온다.")
    void getResource() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(connection);
    }

    @Test
    @DisplayName("Resource를 새롭게 저장한다.")
    void bindResource() {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        Connection resultBeforeStore = TransactionSynchronizationManager.getResource(dataSource);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection resultAfterStore = TransactionSynchronizationManager.getResource(dataSource);

        assertAll(
                () -> assertThat(resultBeforeStore).isNull(),
                () -> assertThat(resultAfterStore).isEqualTo(connection)
        );
    }

    @Test
    @DisplayName("저장된 Resource를 제거한다.")
    void unbindResource() {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection resultBeforeUnbind = TransactionSynchronizationManager.getResource(dataSource);

        TransactionSynchronizationManager.unbindResource(dataSource);
        Connection resultAfterUnbind = TransactionSynchronizationManager.getResource(dataSource);

        assertAll(
                () -> assertThat(resultBeforeUnbind).isEqualTo(connection),
                () -> assertThat(resultAfterUnbind).isNull()
        );
    }
}
