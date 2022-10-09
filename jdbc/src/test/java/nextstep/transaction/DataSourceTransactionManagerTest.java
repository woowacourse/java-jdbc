package nextstep.transaction;

import static nextstep.transaction.support.TransactionIsolation.ISOLATION_DEFAULT;
import static nextstep.transaction.support.TransactionPropagation.PROPAGATION_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataSourceTransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void getTransaction() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionDefinition);

        assertAll(
                () -> assertThat(transaction.getTransactionIsolation()).isEqualTo(ISOLATION_DEFAULT),
                () -> assertThat(transaction.getTransactionPropagation()).isEqualTo(PROPAGATION_REQUIRED),
                () -> assertThat(transaction.getConnection()).isEqualTo(connection)
        );
    }

    @Test
    void commit() throws SQLException {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionDefinition);

        dataSourceTransactionManager.commit(transaction);

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void rollback() throws SQLException {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(defaultTransactionDefinition);

        dataSourceTransactionManager.rollback(transaction);

        verify(connection).rollback();
        verify(connection).close();
    }
}
