package com.techcourse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.service.LoginService;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.ModelAndView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LoginController는")
class LoginControllerTest {

    private HttpServletRequest request;
    private HttpServletResponse response;

    private UserDao userDao;
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
        LoginService loginService = new LoginService(userDao);
        loginController = new LoginController(loginService);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @DisplayName("/login GET 요청시")
    @Nested
    class LoginGet {

        @BeforeEach
        void setUp() {
            when(request.getRequestURI()).thenReturn("/login");
            when(request.getMethod()).thenReturn("GET");
        }

        @DisplayName("이미 로그인한 경우 /index.jsp 이동을 반환한다.")
        @Test
        void alreadyLoggedIn() {
            // given
            when(request.getSession()).thenReturn(mock(HttpSession.class));
            when(request.getSession().getAttribute("user"))
                .thenReturn(new User(1L, "라이언", "비밀번호", "이멜"));

            // when
            ModelAndView modelAndView = loginController.show(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("redirect:/index.jsp");
        }

        @DisplayName("로그인하지 않은 경우 /login.jsp 화면을 반환한다.")
        @Test
        void loginPage() {
            // given
            when(request.getSession()).thenReturn(mock(HttpSession.class));

            // when
            ModelAndView modelAndView = loginController.show(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("/login.jsp");
        }
    }

    @DisplayName("/login POST 요청시")
    @Nested
    class LoginPost {

        @BeforeEach
        void setUp() {
            when(request.getRequestURI()).thenReturn("/login");
            when(request.getMethod()).thenReturn("POST");
        }

        @DisplayName("이미 로그인한 경우 /index.jsp 이동을 반환한다.")
        @Test
        void alreadyLoggedIn() {
            // given
            when(request.getSession()).thenReturn(mock(HttpSession.class));
            when(request.getSession().getAttribute("user"))
                .thenReturn(new User(1L, "라이언", "비밀번호", "이멜"));

            // when
            ModelAndView modelAndView = loginController.login(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("redirect:/index.jsp");
        }

        @DisplayName("로그인에 성공한 경우 /index.jsp 이동을 반환한다.")
        @Test
        void loginSuccess() {
            // given
            userDao.insert(new User("lion", "password", "email"));

            when(request.getSession()).thenReturn(mock(HttpSession.class));
            when(request.getParameter("account")).thenReturn("lion");
            when(request.getParameter("password")).thenReturn("password");

            // when
            ModelAndView modelAndView = loginController.login(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("redirect:/index.jsp");
        }

        @DisplayName("로그인에 실패할 경우 /401.jsp 이동을 반환한다.")
        @Test
        void loginFailed() {
            // given
            when(request.getSession()).thenReturn(mock(HttpSession.class));
            when(request.getParameter("account")).thenReturn("lion");
            when(request.getParameter("password")).thenReturn("password");

            // when
            ModelAndView modelAndView = loginController.login(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("redirect:/401.jsp");
        }
    }

    @DisplayName("/logout POST 요청시")
    @Nested
    class LogoutPost {

        @BeforeEach
        void setUp() {
            when(request.getRequestURI()).thenReturn("/logout");
            when(request.getMethod()).thenReturn("POST");
        }

        @DisplayName("로그아웃 후 최상위 경로 이동을 반환한다.")
        @Test
        void logout() {
            // given
            when(request.getSession()).thenReturn(mock(HttpSession.class));

            // when
            ModelAndView modelAndView = loginController.logout(request, response);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("redirect:/");
        }
    }
}
