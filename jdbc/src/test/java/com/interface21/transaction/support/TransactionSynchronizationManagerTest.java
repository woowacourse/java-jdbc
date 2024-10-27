package com.interface21.transaction.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.interface21.jdbc.core.fixture.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = DataSourceConfig.getInstance();
        connection = dataSource.getConnection();
    }

    @Test
    void 바인딩한_Connection이_반환된다() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // return
        assertEquals(connection, retrievedConnection);
    }

    @Test
    void 바인딩_해제하면_null이_반환된다() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);
        assertEquals(connection, unboundConnection);

        // then
        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);
        assertNull(retrievedConnection);
    }

    @Test
    void 바인딩_안한_상태에선_null이_반환된다() {
        // when & then
        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);
        assertNull(retrievedConnection);
    }
}
