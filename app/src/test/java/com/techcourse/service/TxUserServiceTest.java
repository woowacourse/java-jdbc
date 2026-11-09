package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService appUserService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        appUserService = mock(UserService.class);
        given(dataSource.getConnection()).willReturn(connection);

        txUserService = new TxUserService(appUserService, dataSource);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 비즈니스_로직이_성공하면_바인딩된_커넥션으로_실행하고_커밋한_뒤_정리한다() throws SQLException {
        willAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).given(appUserService).changePassword(1L, "new-password", "gugu");

        txUserService.changePassword(1L, "new-password", "gugu");

        final var inOrder = inOrder(connection, appUserService);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(appUserService).changePassword(1L, "new-password", "gugu");
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 비즈니스_로직과_롤백이_모두_실패하면_원래_예외를_던지고_롤백_예외는_suppressed로_남긴다() throws SQLException {
        final var original = new DataAccessException("이력 저장 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        willThrow(original).given(appUserService).changePassword(1L, "new-password", "gugu");
        willThrow(rollbackFailure).given(connection).rollback();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "new-password", "gugu"))
                .isSameAs(original)
                .satisfies(e -> assertThat(e.getSuppressed()).containsExactly(rollbackFailure));
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
