package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("주어진 DataSource가 등록되어 있다면 매핑된 Connection을 반환한다.")
    void getResource() {
        final DataSource dataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));

        final var actual = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(actual).isInstanceOf(Connection.class);
    }

    @Test
    @DisplayName("주어진 DataSource에 매핑된 Connection이 없으면 null을 반환한다.")
    void getResourceFailed() {
        final DataSource dataSource = mock(DataSource.class);

        final var actual = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("DataSource와 Connection을 매핑한다.")
    void bindResource() {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actual).isEqualTo(connection);
    }

    @Test
    @DisplayName("DataSource에 매핑된 Connection을 매핑 해제한다.")
    void unbindResource() {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection unbindConnection = TransactionSynchronizationManager.unbindResource(dataSource);
        assertThat(unbindConnection).isSameAs(connection);
    }

    @Test
    @DisplayName("DataSource에 매핑된 Connection이 없으면 매핑 해제에 실패한다.")
    void unbindResourceFailed() {
        final DataSource dataSource = mock(DataSource.class);

        final Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);
        assertThat(unboundConnection).isNull();
    }
}