package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;

public class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    public void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
    }

    @AfterEach
    public void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @DisplayName("커넥션을 바인딩한다.")
    @Test
    public void bindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource))
                .isEqualTo(connection);
    }

    @DisplayName("이미 커넥션이 존재하는 경우 예외가 발생한다.")
    @Test
    public void resourceAlreadyBound() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection connection2 = Mockito.mock(Connection.class);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection2))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Transaction already started for this datasource");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(connection);
    }

    @DisplayName("커넥션을 언바인드한다.")
    @Test
    public void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @DisplayName("커넥션이 존재할 때 관리된다고 판단한다.")
    @Test
    public void dataSourceManaged() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertFalse(TransactionSynchronizationManager.doesNotManage(dataSource));
    }

    @DisplayName("커넥션이 존재하지 않을 때 관리되지 않는다고 판단한다.")
    @Test
    public void dataSourceNotManaged() {
        assertTrue(TransactionSynchronizationManager.doesNotManage(dataSource));
    }
}
