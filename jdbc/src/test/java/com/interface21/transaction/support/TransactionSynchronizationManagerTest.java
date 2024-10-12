package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @DisplayName("현재 스레드에 binding된 datasource가 없으면 null을 반환한다")
    @Test
    void getResourceFail() {
        DataSource dataSource = mock(DataSource.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @DisplayName("현재 스레드에 binding된 datasource를 반환한다")
    @Test
    void getResourceSuccess() {
        DataSource dataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, mockConnection);

        Connection actualConnection = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actualConnection).isEqualTo(mockConnection);
    }

    @DisplayName("현재 스레드에 binding된 connection을 제거한다")
    @Test
    void unbindResource() {
        DataSource dataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, mockConnection);

        Connection actualConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        assertAll(
                () -> assertThat(actualConnection).isEqualTo(mockConnection),
                () -> assertThat(actualConnection).isEqualTo(mockConnection)
        );
    }
}
