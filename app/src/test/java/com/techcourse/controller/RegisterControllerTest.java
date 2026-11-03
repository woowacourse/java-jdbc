package com.techcourse.controller;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterControllerTest {
    private final UserDao dao = mock(UserDao.class);
    private final RegisterController controller = new RegisterController(dao);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    void duplicateAccountReturnsConflictWithMessage() {
        doThrow(new DuplicateKeyException("duplicate", null)).when(dao).insert(any(User.class));
        final var result = controller.register(request, response);
        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        assertThat(result.getObject("error")).isEqualTo("이미 사용 중인 아이디입니다.");
    }

    @Test
    void otherDatabaseFailuresAreNotReportedAsDuplicateAccounts() {
        final var failure = new DataAccessException("connection failed");
        doThrow(failure).when(dao).insert(any(User.class));
        assertThatThrownBy(() -> controller.register(request, response)).isSameAs(failure);
        verifyNoInteractions(response);
    }
}
