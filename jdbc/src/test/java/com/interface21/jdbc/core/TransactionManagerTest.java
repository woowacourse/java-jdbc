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

class TransactionManagerTest {

    private DataSource dataSource;
    private TransactionManager transactionManager;

    @BeforeEach
    void setup() {
        this.dataSource = mock(DataSource.class);
        this.transactionManager = new TransactionManager(dataSource);
    }

    @DisplayName("실행을 성공할 경우 커밋한다.")
    @Test
    void should_commitLogic_when_manageSuccessfully() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        List<String> test = new ArrayList<>();
        assertThat(test).hasSize(0);

        // when
        transactionManager.manage(conn -> {
            test.add("new");
        });

        // then
        assertThat(test).hasSize(1);
    }

    @DisplayName("실행을 실패할 경우 롤백한다.")
    @Test
    void should_rollbackLogic_when_manageFail() throws SQLException {
        // given
        when(dataSource.getConnection()).thenThrow(SQLException.class);

        List<String> test = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> {
            transactionManager.manage(conn -> {
                test.add("new");
            });
        }).isInstanceOf(CannotGetJdbcConnectionException.class);
        assertThat(test).hasSize(0);
    }
}
