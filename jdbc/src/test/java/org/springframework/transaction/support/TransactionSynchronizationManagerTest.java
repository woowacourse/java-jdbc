package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    void 두개의_쓰레드로_ThreadLocal_테스트() {
        //첫번째 쓰레드에서 리소스 바인딩
        final DataSource dataSource = DataSourceConfig.getInstance();
        TransactionSynchronizationManager.bindResource(dataSource, getConnection(dataSource));

        //두번째 쓰레드에서 리소스 조회
        new Thread(() -> {
            final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
            assertThat(connection).isNull();
        }).start();
    }

    private Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DataSourceConfig {

        private static javax.sql.DataSource INSTANCE;

        public static javax.sql.DataSource getInstance() {
            if (Objects.isNull(INSTANCE)) {
                INSTANCE = createJdbcDataSource();
            }
            return INSTANCE;
        }

        private static JdbcDataSource createJdbcDataSource() {
            final var jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
            jdbcDataSource.setUser("");
            jdbcDataSource.setPassword("");
            return jdbcDataSource;
        }

        private DataSourceConfig() {
        }
    }
}
