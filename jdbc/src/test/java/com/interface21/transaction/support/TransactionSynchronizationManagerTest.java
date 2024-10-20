package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @DisplayName("리소스 반환 시, 등록된 DataSource가 있으면 Connection 객체를 반환한다.")
    @Test
    void getResourceSuccess() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection actual = TransactionSynchronizationManager.getResource(dataSource);
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isSameAs(connection)
        );
    }

    @DisplayName("리소스 반환 시, 등록된 DataSource가 없으면 null을 반한다.")
    @Test
    void getResourceFailure() {
        Connection actual = TransactionSynchronizationManager.getResource(mock(DataSource.class));
        assertThat(actual).isNull();
    }

    @DisplayName("DataSource와 Connection을 리소스에 등록한다.")
    @Test
    void bindResource() {
        DataSource dataSource = mock(DataSource.class);
        Connection before = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(before).isNull();

        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection after = TransactionSynchronizationManager.getResource(dataSource);
        assertAll(
                () -> assertThat(after).isNotNull(),
                () -> assertThat(after).isSameAs(connection)
        );
    }

    @DisplayName("리소스 제거 시, 등록된 DataSource가 있으면 리소스에서 제거한다.")
    @Test
    void unbindResourceSuccess() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isSameAs(connection)
        );
    }

    @DisplayName("리소스 제거 시, 등록된 DataSource가 없으면 null을 반환한다.")
    @Test
    void unbindResourceFailure() {
        Connection actual = TransactionSynchronizationManager.unbindResource(mock(DataSource.class));
        assertThat(actual).isNull();
    }
}
