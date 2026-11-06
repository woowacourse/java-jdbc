package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService userService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userService = mock(UserService.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        txUserService = new TxUserService(dataSource, userService);
    }

    @Nested
    class ChangePassword {

        @Test
        void 위임하는_동안_커넥션을_바인딩하고_끝나면_해제한다() {
            final AtomicReference<Connection> boundConnection = new AtomicReference<>();
            doAnswer(invocation -> {
                boundConnection.set(TransactionSynchronizationManager.getResource(dataSource));
                return null;
            }).when(userService).changePassword(1L, "newPassword", "gugu");

            txUserService.changePassword(1L, "newPassword", "gugu");

            assertThat(boundConnection.get()).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        void 커밋한_뒤_원래_autoCommit_상태로_복구하고_커넥션을_닫는다() throws SQLException {
            txUserService.changePassword(1L, "newPassword", "gugu");

            final InOrder inOrder = inOrder(connection, userService);
            inOrder.verify(connection).setAutoCommit(false);
            inOrder.verify(userService).changePassword(1L, "newPassword", "gugu");
            inOrder.verify(connection).commit();
            inOrder.verify(connection).setAutoCommit(true);
            inOrder.verify(connection).close();
        }

        @Test
        void 롤백한_뒤에도_원래_autoCommit_상태로_복구하고_커넥션을_닫는다() throws SQLException {
            doThrow(new DataAccessException()).when(userService).changePassword(1L, "newPassword", "gugu");

            assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                    .isInstanceOf(DataAccessException.class);

            final InOrder inOrder = inOrder(connection);
            inOrder.verify(connection).setAutoCommit(false);
            inOrder.verify(connection).rollback();
            inOrder.verify(connection).setAutoCommit(true);
            inOrder.verify(connection).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        void autoCommit_복구에_실패해도_바인딩을_해제하고_커넥션을_닫는다() throws SQLException {
            doThrow(new SQLException("restore failed")).when(connection).setAutoCommit(true);

            txUserService.changePassword(1L, "newPassword", "gugu");

            verify(connection).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }
    }

    @Nested
    class Delegate {

        @Test
        void findById는_트랜잭션_없이_그대로_위임한다() throws SQLException {
            final User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
            when(userService.findById(1L)).thenReturn(user);

            final User actual = txUserService.findById(1L);

            assertThat(actual).isSameAs(user);
            verify(dataSource, never()).getConnection();
        }

        @Test
        void save는_트랜잭션_없이_그대로_위임한다() throws SQLException {
            final User user = new User("gugu", "password", "hkkang@woowahan.com");

            txUserService.save(user);

            verify(userService).save(user);
            verify(dataSource, never()).getConnection();
        }
    }
}
