package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import nextstep.jdbc.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class TxUserServiceTest {

    private PlatformTransactionManager platformTransactionManager;
    private TransactionStatus transactionStatus;
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.platformTransactionManager = mock(PlatformTransactionManager.class);
        this.transactionStatus = mock(TransactionStatus.class);
        this.userService = mock(UserService.class);
    }

    @DisplayName("트랜잭션이 커밋된다.")
    @Test
    void changePassword_commits() {
        // given
        final UserService txUserService = new TxUserService(platformTransactionManager, userService);
        when(platformTransactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);

        // when
        txUserService.changePassword(1L, "newPassword", "sun");

        // then
        verify(platformTransactionManager).commit(transactionStatus);
    }

    @DisplayName("트랜잭션이 롤백된다.")
    @Test
    void changePassword_rollbacks_ifExceptionThrown() {
        // given
        final UserService txUserService = new TxUserService(platformTransactionManager, userService);
        when(platformTransactionManager.getTransaction(any(DefaultTransactionDefinition.class)))
                .thenReturn(transactionStatus);
        doThrow(new DataAccessException())
                .when(userService)
                .changePassword(anyLong(), anyString(), anyString());

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "sun"))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(platformTransactionManager).rollback(transactionStatus)
        );
    }
}
