package aop.stage0;

import aop.DataAccessException;
import aop.domain.User;
import aop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionHandlerTest {

    private UserService target;
    private UserService userService;

    @BeforeEach
    void setUp() {
        target = mock(UserService.class);
        userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class<?>[]{UserService.class},
                new TransactionHandler(target)
        );
    }

    @Test
    void 프록시가_실제_서비스에_인자를_전달하고_반환값을_돌려준다() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        when(target.findById(1L)).thenReturn(user);

        final var actual = userService.findById(1L);

        assertThat(actual).isSameAs(user);
        verify(target).findById(1L);
    }

    @Test
    void 프록시가_실제_서비스에서_발생한_예외를_그대로_전달한다() {
        final var exception = new DataAccessException();
        doThrow(exception).when(target).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(exception);
    }
}
