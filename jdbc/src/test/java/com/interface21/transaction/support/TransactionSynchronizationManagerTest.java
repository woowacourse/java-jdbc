package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 바인딩한_Connection을_조회할_수_있다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 언바인딩하면_더_이상_조회되지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var actual = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 같은_DataSource에_중복_바인딩하면_예외가_발생한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 다른_스레드에서는_바인딩한_Connection이_보이지_않는다() throws InterruptedException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final Connection[] result = new Connection[1];

        final Thread thread = new Thread(() -> result[0] = TransactionSynchronizationManager.getResource(dataSource));
        thread.start();
        thread.join();

        assertThat(result[0]).isNull();
    }
}
