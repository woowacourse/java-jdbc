package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.TestDataSourceConfig;

class TransactionSynchronizationManagerTest {

    @Test
    void 쓰레드별로_동일한_데이터소스를_가져도_서로_다른_리소스를_가진다() throws InterruptedException {
        DataSource dataSource = TestDataSourceConfig.getInstance();
        new Thread(() -> {
            try {
                TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection()); // 한 쓰레드에서 connection 바인딩
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Connection resource = TransactionSynchronizationManager.getResource(dataSource);
            assertThat(resource).isNotNull();
        }).start();

        Thread.sleep(500);
        assertThatThrownBy(() -> TransactionSynchronizationManager.getResource(dataSource)) // 동일한 키 값으로 조회하더라도 쓰레드가 달라서 NPE 발생
                .isInstanceOf(NullPointerException.class);
    }
}
