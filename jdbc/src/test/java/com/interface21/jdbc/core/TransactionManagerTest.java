package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    @DisplayName("트랜잭션 내에서 작업을 수행한다.")
    @Test
    void doInTransactionTest() throws Exception {
        // given
        Connection connection = spy(MockConnection.class);
        DataSource dataSource = mock(DataSource.class);
        when(DataSourceUtils.getConnection(dataSource)).thenReturn(connection);

        // when
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        executorService.submit(() -> TransactionManager.doInTransaction(dataSource, () -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        // then
        Thread.sleep(100);
        assertThat(connection.getAutoCommit()).isFalse();

        countDownLatch.countDown();
        Thread.sleep(100);
        assertThat(connection.getAutoCommit()).isTrue();

        executorService.shutdown();
    }

    @DisplayName("트랜잭션 내에서 예외가 발생하면 롤백한다.")
    @Test
    void doInTransactionTest1() throws Exception {
        // given
        Connection connection = spy(MockConnection.class);
        DataSource dataSource = mock(DataSource.class);
        when(DataSourceUtils.getConnection(dataSource)).thenReturn(connection);

        // when
        assertThatThrownBy(() -> TransactionManager.doInTransaction(dataSource, () -> {
            throw new RuntimeException("예상치 못한 예외가 발생했습니다.");
        })).isInstanceOf(DataAccessException.class);

        // then
        verify(connection).rollback();
    }

    private static abstract class MockConnection implements Connection {

        protected boolean autoCommit = true;

        @Override
        public void setAutoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
        }

        @Override
        public boolean getAutoCommit() {
            return autoCommit;
        }
    }
}
