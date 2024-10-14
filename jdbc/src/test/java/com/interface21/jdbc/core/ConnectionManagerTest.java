package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionManagerTest {

    private DataSource dataSource;
    private ConnectionManager connectionManager;

    @BeforeEach
    void setup() {
        this.dataSource = mock(DataSource.class);
        this.connectionManager = new ConnectionManager(dataSource);
    }

    @DisplayName("파라미터로 전달한 로직을 성공적으로 실행한다.")
    @Test
    void success() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        List<String> test = new ArrayList<>();
        assertThat(test).hasSize(0);

        // when
        connectionManager.manage(conn -> {
            test.add("new");
        });

        // then
        assertThat(test).hasSize(1);
    }

    @DisplayName("Connection 가져올 때 실패할 경우 예외가 발생한다.")
    @Test
    void fail() throws SQLException {
        // given
        when(dataSource.getConnection()).thenThrow(SQLException.class);

        List<String> test = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> {
            connectionManager.manage(conn -> {
                test.add("new");
            });
        }).isInstanceOf(CannotGetJdbcConnectionException.class);
        assertThat(test).hasSize(0);
    }
}
