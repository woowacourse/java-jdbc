package org.springframework.transaction.support;

import com.techcourse.config.DataSourceConfig;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSynchronizationManagerTest {

    @Test
    void 두개의_쓰레드에서_커넥션_가져오기() throws SQLException {
        final DataSource dataSource = DataSourceConfig.getInstance();
        TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());

        new Thread(
                () -> {
                    final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
                    assertThat(connection).isNotNull();
                }
        );
    }
}
