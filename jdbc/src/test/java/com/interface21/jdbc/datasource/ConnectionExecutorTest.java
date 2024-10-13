package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionExecutorTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
    }

    @DisplayName("입력된 Consumer를 하나의 트랜잭션 안에서 처리한다.")
    @Test
    void executeTransactional() throws SQLException {
        // given
        List<Integer> list = new ArrayList<>();

        // when
        ConnectionExecutor.executeTransactional(connection, conn -> addZero(connection, list));

        // then
        assertThat(list).contains(0);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @DisplayName("예외가 발생하면 롤백한다.")
    @Test
    void executeTransactional_rollback() throws SQLException {
        // when
        assertThatThrownBy(
                () -> ConnectionExecutor.executeTransactional(connection, conn -> throwException(connection)))
                .isInstanceOf(DataAccessException.class);

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @DisplayName("입력된 Consumer를 처리하고 커넥션을 닫는다.")
    @Test
    void execute() throws SQLException {
        // given
        List<Integer> list = new ArrayList<>();

        // when
        ConnectionExecutor.execute(connection, conn -> addZero(connection, list));

        // then
        assertThat(list).contains(0);
        verify(connection).close();
    }

    @DisplayName("입력된 Function을 처리하고 커넥션을 닫는다.")
    @Test
    void apply() throws SQLException {
        // when
        int value = ConnectionExecutor.apply(connection, this::getZero);

        // then
        assertThat(value).isEqualTo(0);
        verify(connection).close();
    }

    private void addZero(Connection connection, List<Integer> list) {
        list.add(0);
    }

    private void throwException(Connection connection) {
        throw new RuntimeException();
    }

    private int getZero(Connection connection) {
        return 0;
    }
}
