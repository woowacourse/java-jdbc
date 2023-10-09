package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionSynchronizationManagerTest {

    DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = Mockito.mock(DataSource.class);
    }

    @Test
    void getResourceTest_when_datasource_not_exist() {
        final Connection resource = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(resource).isNull();
    }

    @Test
    void getResourceTest_when_datasource_exist() {
        // given
        final Connection expected = Mockito.mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, expected);

        // when
        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void unbindResource_success() {
        // given
        final Connection connection = Mockito.mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();

        // when
        TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void unbindResource_fail_when_no_dataSource() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isActualTransactionActive_false() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }

    @Test
    void isActualTransactionActive_true() {
        // given
        TransactionSynchronizationManager.setActualTransactionActiveTrue();

        // then
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
    }
}
