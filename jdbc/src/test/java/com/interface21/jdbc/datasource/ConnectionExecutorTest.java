package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionExecutorTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("입력된 Consumer를 하나의 트랜잭션 안에서 처리한다.")
    @Test
    void executeTransactional() throws SQLException {
        // given
        List<Integer> list = new ArrayList<>();

        // when
        ConnectionExecutor.executeTransactional(dataSource, () -> addZero(list));

        // then
        assertThat(list).contains(0);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
    }

    @DisplayName("예외가 발생하면 롤백한다.")
    @Test
    void executeTransactional_rollback() throws SQLException {
        // when
        assertThatThrownBy(
                () -> ConnectionExecutor.executeTransactional(dataSource, this::throwException))
                .isInstanceOf(DataAccessException.class);

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).close();
    }

    @DisplayName("입력된 Consumer를 처리하고 커넥션을 닫는다.")
    @Test
    void execute() throws SQLException {
        // given
        List<Integer> list = new ArrayList<>();

        // when
        ConnectionExecutor.execute(dataSource, () -> addZero(list));

        // then
        assertThat(list).contains(0);
        verify(connection).close();
    }

    @DisplayName("입력된 Function을 처리하고 커넥션을 닫는다.")
    @Test
    void supply() throws SQLException {
        // when
        int value = ConnectionExecutor.supply(dataSource, this::getZero);

        // then
        assertThat(value).isEqualTo(0);
        verify(connection).close();
    }

    private void addZero(List<Integer> list) {
        list.add(0);
    }

    private void throwException() {
        throw new RuntimeException();
    }

    private int getZero() {
        return 0;
    }
}

