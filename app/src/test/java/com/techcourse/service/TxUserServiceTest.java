package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class TxUserServiceTest {

    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final TxUserService txUserService = new TxUserService(transactionManager, appUserService);

    @DisplayName("비밀번호를 변경에 성공한 후 트랜잭션을 커밋한다.")
    @Test
    void changePassword_commit() {
        // given
        doNothing().when(appUserService)
                .changePassword(anyLong(), anyString(), anyString());

        // when
        txUserService.changePassword(1L, "password", "createBy");

        // then
        verify(transactionManager).commit(any());
    }

    @DisplayName("비밀번호를 변경하던 중 예외가 발생하면 트랜잭션을 롤백한다.")
    @Test
    void changePassword_rollback() {
        // given
        doThrow(new RuntimeException()).when(appUserService)
                .changePassword(anyLong(), anyString(), anyString());

        // when, then
        assertAll(
                () -> assertThatExceptionOfType(RuntimeException.class)
                        .isThrownBy(() -> txUserService.changePassword(1L, "password", "createBy")),
                () -> verify(transactionManager).rollback(any())
        );
    }
}
