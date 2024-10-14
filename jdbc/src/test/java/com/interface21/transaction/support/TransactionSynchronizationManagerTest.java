package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("데이터 소스에 해당하는 커넥션을 바인딩한다.")
    void getConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actual).isEqualTo(connection);
    }

    @Test
    @DisplayName("데이터 소스에 해당하는 커넥션을 언바인딩한다.")
    void releaseConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection unbindResource = TransactionSynchronizationManager.unbindResource(dataSource);
        Connection foundResource = TransactionSynchronizationManager.getResource(dataSource);

        assertAll(
            () -> assertThat(unbindResource).isEqualTo(connection),
            () -> assertThat(foundResource).isNull()
        );
        unbindResource.close();
    }
}
