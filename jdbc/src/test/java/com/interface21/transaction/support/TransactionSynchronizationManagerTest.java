package com.interface21.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

// 매니저의 상태는 static ThreadLocal이라 같은 스레드에서 도는 테스트끼리 공유된다.
// 테스트마다 새 mock DataSource를 키로 쓰므로, 앞선 테스트가 남긴 바인딩과 키가 겹치지 않는다.
class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    // 다른 스레드에서 작업을 실행하고 결과를 돌려받는다. 그 스레드에서 난 예외는 ExecutionException으로 감싸져 테스트를 실패시킨다.
    private static <T> T runInAnotherThread(final Callable<T> task) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(task).get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @Nested
    @DisplayName("같은 스레드 안에서 바인딩한 Connection을 찾는다")
    class SameThread {

        @Test
        @DisplayName("바인딩한 적 없는 DataSource로 찾으면 null을 반환한다")
        void getWithoutBinding() {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        @DisplayName("바인딩한 Connection을 같은 DataSource로 찾으면, 몇 번을 찾든 그 Connection을 그대로 반환한다")
        void getAfterBind() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }

        @Test
        @DisplayName("unbind는 바인딩돼 있던 Connection을 반환한다")
        void unbindReturnsBoundConnection() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        }

        @Test
        @DisplayName("unbind한 뒤에는 같은 DataSource로 찾아도 null을 반환한다")
        void getAfterUnbind() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        // 톰캣처럼 스레드를 재사용하면, 한 스레드에서 트랜잭션이 끝난 뒤 다음 트랜잭션이 또 시작된다.
        @Test
        @DisplayName("unbind한 뒤 같은 DataSource에 새 Connection을 다시 바인딩하면, 새 Connection을 찾는다")
        void rebindAfterUnbind() {
            final Connection nextConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.unbindResource(dataSource);

            TransactionSynchronizationManager.bindResource(dataSource, nextConnection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(nextConnection);
        }
    }

    @Nested
    @DisplayName("DataSource마다 따로 관리한다")
    class PerDataSource {

        private DataSource otherDataSource;
        private Connection otherConnection;

        @BeforeEach
        void setUp() {
            otherDataSource = mock(DataSource.class);
            otherConnection = mock(Connection.class);
        }

        @Test
        @DisplayName("서로 다른 DataSource에 바인딩한 Connection은 각자의 DataSource로만 찾는다")
        void separatedByDataSource() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        }

        @Test
        @DisplayName("한 DataSource를 unbind해도 다른 DataSource의 바인딩은 그대로 남는다")
        void unbindKeepsOtherDataSource() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

            TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        }
    }

    // 요청마다 스레드가 다르다. 한 요청의 트랜잭션 Connection이 다른 요청의 DAO에 새어 나가면 안 된다.
    @Nested
    @DisplayName("스레드마다 따로 관리한다")
    class PerThread {

        @Test
        @DisplayName("이 스레드에서 바인딩한 Connection은 다른 스레드에서 보이지 않는다")
        void notVisibleFromAnotherThread() throws Exception {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection seenByAnotherThread = runInAnotherThread(
                    () -> TransactionSynchronizationManager.getResource(dataSource));

            assertThat(seenByAnotherThread).isNull();
        }

        @Test
        @DisplayName("다른 스레드에서 바인딩한 Connection은 이 스레드에서 보이지 않는다")
        void anotherThreadBindingNotVisibleHere() throws Exception {
            runInAnotherThread(() -> {
                TransactionSynchronizationManager.bindResource(dataSource, connection);
                return null;
            });

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        @DisplayName("두 스레드가 같은 DataSource에 각자 다른 Connection을 바인딩해도 서로 덮어쓰지 않는다")
        void sameDataSourceDifferentThreads() throws Exception {
            final Connection anotherThreadConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection seenByAnotherThread = runInAnotherThread(() -> {
                TransactionSynchronizationManager.bindResource(dataSource, anotherThreadConnection);
                return TransactionSynchronizationManager.getResource(dataSource);
            });

            assertThat(seenByAnotherThread).isSameAs(anotherThreadConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    // 같은 키로 덮어쓰면 앞 Connection은 커밋, 롤백, close 없이 사라진다. 그래서 덮어쓰지 않고 바로 실패한다.
    @Nested
    @DisplayName("같은 스레드에서 이미 바인딩된 DataSource에는 다시 bind할 수 없다")
    class DuplicateBind {

        @Test
        @DisplayName("이미 바인딩된 DataSource에 다른 Connection을 bind하면 IllegalStateException을 던진다")
        void bindAnotherConnection() {
            final Connection anotherConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, anotherConnection))
                    .isInstanceOf(IllegalStateException.class);
        }

        // 중복 여부는 Connection이 아니라 DataSource 키로 판단한다.
        @Test
        @DisplayName("이미 바인딩된 것과 같은 Connection을 다시 bind해도 IllegalStateException을 던진다")
        void bindSameConnectionAgain() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("중복 bind가 실패해도 기존 Connection의 바인딩은 그대로 남는다")
        void keepsOriginalBindingAfterFailure() {
            final Connection anotherConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, anotherConnection))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    // 바인딩이 없는데 unbind한다는 건 키를 잘못 넣었거나, 이미 정리한 걸 또 정리한다는 뜻이다. 조용히 넘어가지 않고 바로 드러낸다.
    @Nested
    @DisplayName("바인딩되지 않은 DataSource는 unbind할 수 없다")
    class UnbindWithoutBinding {

        @Test
        @DisplayName("바인딩한 적 없는 DataSource를 unbind하면 IllegalStateException을 던진다")
        void unbindNeverBound() {
            assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 unbind한 DataSource를 다시 unbind하면 IllegalStateException을 던진다")
        void unbindTwice() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.unbindResource(dataSource);

            assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("다른 DataSource만 바인딩돼 있을 때 이 DataSource를 unbind하면 예외를 던지고, 다른 DataSource의 바인딩은 건드리지 않는다")
        void unbindWrongKey() {
            final DataSource otherDataSource = mock(DataSource.class);
            final Connection otherConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

            assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        }

        @Test
        @DisplayName("다른 스레드에서는 이 스레드의 바인딩을 unbind할 수 없고, 이 스레드의 바인딩은 그대로 남는다")
        void unbindFromAnotherThread() throws Exception {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Throwable thrownInAnotherThread = runInAnotherThread(
                    () -> catchThrowable(() -> TransactionSynchronizationManager.unbindResource(dataSource)));

            assertThat(thrownInAnotherThread).isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    @Nested
    @DisplayName("null Connection은 bind할 수 없다")
    class NullConnection {

        @Test
        @DisplayName("null Connection을 bind하면 IllegalArgumentException을 던진다")
        void bindNull() {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        // 실패한 bind가 흔적을 남기면, 이후의 중복 검사나 unbind 검사가 엉뚱하게 걸릴 수 있다.
        @Test
        @DisplayName("null bind가 실패한 뒤에도 그 DataSource는 바인딩되지 않은 상태라, 정상 Connection을 bind할 수 있다")
        void bindAfterNullRejected() {
            catchThrowable(() -> TransactionSynchronizationManager.bindResource(dataSource, null));

            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }
}
