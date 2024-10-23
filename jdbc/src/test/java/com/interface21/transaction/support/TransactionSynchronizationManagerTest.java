package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.utils.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    DataSource dataSource = DataSourceConfig.getInstance();

    @DisplayName("같은 스레드 내에서는 같은 커넥션을 공유한다.")
    @Test
    void bindConnection() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());

        // when
        Connection connection1 = TransactionSynchronizationManager.getResource(dataSource);
        Connection connection2 = TransactionSynchronizationManager.getResource(dataSource);

        TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(connection1).isEqualTo(connection2);
    }

    @DisplayName("서로 다른 스레드는 다른 커넥션을 사용한다.")
    @Test
    void differentConnection() throws SQLException, InterruptedException {
        List<Connection> connections = new ArrayList<>();

        // when
        TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());
        Connection connection1 = TransactionSynchronizationManager.getResource(dataSource);
        connections.add(connection1);
        TransactionSynchronizationManager.unbindResource(dataSource);

        Thread thread = new Thread(() -> {
            try {
                TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());
                Connection connection2 = TransactionSynchronizationManager.getResource(dataSource);
                connections.add(connection2);
                TransactionSynchronizationManager.unbindResource(dataSource);
            } catch (SQLException ignored) {}
        });
        thread.start();
        thread.join();

        // then
        assertThat(connections.get(0)).isNotEqualTo(connections.get(1));
    }
}
