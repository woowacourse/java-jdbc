package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.H2DataSourceConfig;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    @DisplayName("기존의 커넥션이 존재할 경우, 기존의 커넥션을 가져올 수 있다.")
    @Test
    void getExistingConnection() throws SQLException {
        // given
        DataSource dataSource = H2DataSourceConfig.getInstance();
        Connection expetedConnection = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, expetedConnection);

        // when
        Connection actualConnection = DataSourceUtils.getConnection(dataSource);

        // then
        assertThat(expetedConnection).isEqualTo(actualConnection);
    }

    @DisplayName("기존의 커넥션이 존재하지 않을 경우, 새로운 커넥션을 가져온다.")
    @Test
    void getNewConnection() {
        // given
        DataSource dataSource = H2DataSourceConfig.getInstance();

        // when
        Connection connection = DataSourceUtils.getConnection(dataSource);

        // then
        assertThat(connection).isNotNull();
    }

    @DisplayName("기존의 커넥션들을 방출시킬 수 있다.")
    @Test
    void releaseExistingConnection() throws SQLException {
        // given
        DataSource dataSource = H2DataSourceConfig.getInstance();
        Connection connection = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);
        Connection resource = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(resource).isNull();
    }
}
