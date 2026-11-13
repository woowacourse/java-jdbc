package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection conn;
    private UserService appUserService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        appUserService = mock(UserService.class);

        when(dataSource.getConnection()).thenReturn(conn);

        txUserService = new TxUserService(appUserService, dataSource);
    }

    @DisplayName("비밀번호 변경에 실패하면 롤백하고 커넥션을 정리한다")
    @Test
    void rollbackWhenChangePasswordFails() throws SQLException {
        doThrow(new DataAccessException()).when(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(conn).rollback();
        verify(conn, never()).commit();
        verify(conn).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @DisplayName("롤백도 실패하면 처음 예외를 던지고 롤백 예외는 suppressed로 남긴다")
    @Test
    void keepCauseWhenRollbackFails() throws SQLException {
        DataAccessException cause = new DataAccessException("이력 저장 실패");
        SQLException rollbackException = new SQLException("롤백 실패");
        doThrow(cause).when(appUserService).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackException).when(conn).rollback();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(cause)
                .hasSuppressedException(rollbackException);
    }
}
