package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.web.bind.annotation.RequestParam;
import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String view(HttpServletRequest req) {
        return UserSession.getUserFrom(req.getSession())
                .map(user -> {
                    log.info("logged in {}", user.getAccount());
                    return "redirect:/index.jsp";
                })
                .orElse("/login.jsp");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            HttpServletRequest req
    ) {
        if (UserSession.isLoggedIn(req.getSession())) {
            return "redirect:/index.jsp";
        }

        return InMemoryUserRepository.findByAccount(account)
                .map(user -> {
                    log.info("User : {}", user);
                    return checkLogin(req, user, password);
                })
                .orElse("redirect:/401.jsp");
    }

    private String checkLogin(HttpServletRequest request, User user, String password) {
        if (user.checkPassword(password)) {
            HttpSession session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return "redirect:/index.jsp";
        }
        return "redirect:/401.jsp";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(UserSession.SESSION_KEY);
        return "redirect:/";
    }
}
