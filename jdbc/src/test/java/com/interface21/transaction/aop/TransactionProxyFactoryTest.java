package com.interface21.transaction.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.transaction.annotation.Transactional;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TransactionProxyFactoryTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    @DisplayName("@Transactional 메서드는 바인딩된 커넥션 위에서 실행되고 커밋된다")
    void commitsTransactionalMethod() throws SQLException {
        final FakeService proxy = TransactionProxyFactory.createProxy(new FakeService(dataSource), dataSource);

        final Connection used = proxy.transactional();

        assertThat(used).isSameAs(connection);
        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("@Transactional 메서드에서 예외가 나면 롤백하고 예외를 그대로 던진다")
    void rollsBackWhenExceptionOccurs() throws SQLException {
        final FakeService proxy = TransactionProxyFactory.createProxy(new FakeService(dataSource), dataSource);

        assertThatThrownBy(proxy::failing).isInstanceOf(IllegalStateException.class);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("@Transactional이 없는 메서드에는 트랜잭션을 걸지 않는다")
    void doesNotStartTransactionForPlainMethod() throws SQLException {
        final FakeService proxy = TransactionProxyFactory.createProxy(new FakeService(dataSource), dataSource);

        assertThat(proxy.plain()).isNull();

        verify(dataSource, never()).getConnection();
    }

    static class FakeService {

        private final DataSource dataSource;

        FakeService(final DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Transactional
        public Connection transactional() {
            return TransactionSynchronizationManager.getResource(dataSource);
        }

        @Transactional
        public void failing() {
            throw new IllegalStateException("실패");
        }

        public Connection plain() {
            return TransactionSynchronizationManager.getResource(dataSource);
        }
    }
}
