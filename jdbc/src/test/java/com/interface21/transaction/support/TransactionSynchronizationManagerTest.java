package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @DisplayName("같은 DataSource에 커넥션이 이미 바인딩되어 있으면 예외를 던지고 기존 바인딩을 유지한다")
    @Test
    void bindTwice() {
        Connection first = mock(Connection.class);
        Connection second = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, first);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, second))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(first);
    }
}
