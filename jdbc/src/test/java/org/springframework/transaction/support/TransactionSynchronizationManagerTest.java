package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.TestDataSourceConfig;

class TransactionSynchronizationManagerTest {

    @Test
    void 저장된_데이터소스가_없는_경우_null이_반환된다() {
        Connection connection = TransactionSynchronizationManager.getResource(TestDataSourceConfig.getInstance());
        assertThat(connection).isNull();
    }

    @Test
    void 쓰레드별로_동일한_데이터소스를_가져도_서로_다른_리소스를_가진다() throws InterruptedException {
        DataSource dataSource = TestDataSourceConfig.getInstance();
        new Thread(() -> {
            try {
                TransactionSynchronizationManager.bindResource(dataSource,
                        dataSource.getConnection()); // 한 쓰레드에서 connection 바인딩
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Connection resource = TransactionSynchronizationManager.getResource(dataSource);
            assertThat(resource).isNotNull();
        }).start();

        Thread.sleep(500);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull(); // 동일한 키 값으로 조회하더라도 쓰레드가 달라서 null 발생
    }
}
