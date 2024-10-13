package com.techcourse.support.jdbc.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionExecutorUtilsTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        DatabasePopulatorUtils.execute(dataSource);
    }

    @DisplayName("connection이 열려 있는 경우 자원을 반환한다.")
    @Test
    void releaseActiveConn() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.updateWithActiveConn(sql, "jojo", "password", "jojo@email.com");
        Connection beforeConn = TransactionSynchronizationManager.getResource(dataSource);

        TransactionExecutorUtils.releaseActiveConn();
        Connection afterConn = TransactionSynchronizationManager.getResource(dataSource);

        assertAll(
                () -> assertThat(beforeConn).isNotNull(),
                () -> assertThat(afterConn).isNull()
        );
    }

    @DisplayName("connection이 닫혀 있는 경우 자원을 반환하지 않는다.")
    @Test
    void releaseInActiveConn() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "jojo", "password", "jojo@email.com");
        Connection beforeConn = TransactionSynchronizationManager.getResource(dataSource);

        TransactionExecutorUtils.releaseActiveConn();
        Connection afterConn = TransactionSynchronizationManager.getResource(dataSource);

        assertAll(
                () -> assertThat(beforeConn).isNull(),
                () -> assertThat(afterConn).isNull()
        );
    }
}
