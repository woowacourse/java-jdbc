package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AutocommitConnectionTest {

    @DisplayName("커넥션이 재사용될 경우, HikariPool은 autocomit의 상태를 물려 받지 않는다.")
    @Test
    void connectionAutoCommit() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        config.setUsername("");
        config.setMaximumPoolSize(1);

        DataSource dataSource = new HikariDataSource(config);

        Connection connection1 = dataSource.getConnection();
        connection1.setAutoCommit(false);
        connection1.close();

        Connection connection2 = dataSource.getConnection();
        assertThat(connection2.getAutoCommit()).isTrue();
    }
}
