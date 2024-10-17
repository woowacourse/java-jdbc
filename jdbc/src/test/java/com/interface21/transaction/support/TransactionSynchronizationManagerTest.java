package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.H2DataSourceConfig;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @DisplayName("다른 쓰레드끼리 커넥션을 공유하지 않는다.")
    @Test
    void notEqualConnectionWhenOtherThread() throws SQLException, InterruptedException {
        // given
        List<Connection> connections = new ArrayList<>();

        DataSource dataSource = H2DataSourceConfig.getInstance();
        TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());

        // when
        Thread thread = new Thread(() -> {
            try {
                Connection connection2 = dataSource.getConnection();
                TransactionSynchronizationManager.bindResource(dataSource, connection2);
                connections.add(connection2);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        thread.join();

        Connection connection1 = TransactionSynchronizationManager.getResource(dataSource);
        connections.add(connection1);

        // then
        assertThat(connections.get(0)).isNotEqualTo(connections.get(1));
    }
}
