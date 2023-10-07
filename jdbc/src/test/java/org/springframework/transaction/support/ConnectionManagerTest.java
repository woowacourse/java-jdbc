package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;

class ConnectionManagerTest {

    @Nested
    @DisplayName("datasource로 connection을 맞는다.")
    class getConnection {

        @Test
        @DisplayName("스레드에서 connection을 생성한 적이 없다면, tx를 생성한다.")
        void getNewConnection() throws SQLException {
            final ConnectionManager txManager = new ConnectionManager();

            final Connection connection = txManager.getConnection(DataSourceConfig.getInstance());

            assertThat(connection)
                .isNotNull();
        }

        @Test
        @DisplayName("스레드에서 connection을 생성한 적이 있다면, 기존 tx를 반환한다.")
        void getOldConnection() throws SQLException {
            final ConnectionManager txManager = new ConnectionManager();
            final Connection oldTransaction = txManager.getConnection(
                DataSourceConfig.getInstance());

            final Connection newTransaction = txManager.getConnection(
                DataSourceConfig.getInstance());

            assertThat(oldTransaction)
                .isEqualTo(newTransaction);
        }
    }
}
