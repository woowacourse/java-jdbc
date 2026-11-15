package com.interface21.transaction.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.transaction.annotation.Transactional;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionProxyFactoryTest {

    private DataSource dataSource;
    private Connection connection;
    private TestService target;
    private Service service;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        target = new TestService(dataSource);
        service = TransactionProxyFactory.create(Service.class, target, dataSource);
    }

    @Test
    void appliesTargetAnnotationAndPreservesArgumentsAndReturnValue() throws SQLException {
        assertThat(service.greet("gugu")).isEqualTo("Hello, gugu");

        assertThat(target.observedConnection).isSameAs(connection);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void appliesInterfaceAnnotation() throws SQLException {
        assertThat(service.viaInterface()).isEqualTo("interface");

        assertThat(target.observedConnection).isSameAs(connection);
        verify(connection).commit();
    }

    @Test
    void bypassesTransactionForUnannotatedMethod() {
        assertThat(service.plain()).isEqualTo("plain");

        verifyNoInteractions(dataSource, connection);
    }

    @Test
    void preservesOriginalRuntimeExceptionAndRollsBack() throws SQLException {
        final var failure = new IllegalStateException("runtime failure");

        assertThat(assertThrows(IllegalStateException.class, () -> service.fail(failure))).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesOriginalErrorAndRollsBack() throws SQLException {
        final var failure = new AssertionError("error failure");

        assertThat(assertThrows(AssertionError.class, () -> service.failWithError(failure))).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void preservesDeclaredCheckedExceptionAndRollsBack() throws SQLException {
        final var failure = new IOException("checked failure");

        assertThat(assertThrows(IOException.class, () -> service.failChecked(failure))).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void retainsCleanupFailureOnOriginalCheckedException() throws SQLException {
        final var failure = new IOException("checked failure");
        final var rollbackFailure = new SQLException("rollback failure");
        doThrow(rollbackFailure).when(connection).rollback();

        final var thrown = assertThrows(IOException.class, () -> service.failChecked(failure));

        assertThat(thrown).isSameAs(failure);
        assertThat(thrown.getSuppressed()).anyMatch(
                suppressed -> suppressed == rollbackFailure || suppressed.getCause() == rollbackFailure);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void usesProxyIdentityForObjectMethodsWithoutStartingTransaction() {
        final var anotherProxy = TransactionProxyFactory.create(Service.class, target, dataSource);

        assertThat(service.equals(service)).isTrue();
        assertThat(service.equals(target)).isFalse();
        assertThat(service.equals(anotherProxy)).isFalse();
        assertThat(service.equals(null)).isFalse();
        assertThat(service.hashCode()).isEqualTo(System.identityHashCode(service));
        assertThat(service.toString()).contains("Transactional proxy for ", target.toString());
        verifyNoInteractions(dataSource, connection);
    }

    public interface Service {

        String greet(String name);

        @Transactional
        String viaInterface();

        String plain();

        void fail(RuntimeException failure);

        void failWithError(Error failure);

        void failChecked(IOException failure) throws IOException;
    }

    private static final class TestService implements Service {

        private final DataSource dataSource;
        private Connection observedConnection;

        private TestService(final DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        @Transactional
        public String greet(final String name) {
            observedConnection = TransactionSynchronizationManager.getResource(dataSource);
            return "Hello, " + name;
        }

        @Override
        public String viaInterface() {
            observedConnection = TransactionSynchronizationManager.getResource(dataSource);
            return "interface";
        }

        @Override
        public String plain() {
            return "plain";
        }

        @Override
        @Transactional
        public void fail(final RuntimeException failure) {
            throw failure;
        }

        @Override
        @Transactional
        public void failWithError(final Error failure) {
            throw failure;
        }

        @Override
        @Transactional
        public void failChecked(final IOException failure) throws IOException {
            throw failure;
        }
    }
}
