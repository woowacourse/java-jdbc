package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserSession;
import com.techcourse.exception.LoginFailException;
import com.techcourse.exception.RegisterFailException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.Pages;
import nextstep.mvc.exception.BadRequestException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Service;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findByAccount(String account) {
        try {
            return userDao.findByAccount(account);
        } catch (BadRequestException e) {
            throw new LoginFailException();
        }
    }

    public Pages checkedLogin(HttpServletRequest request) {
        User user = findByAccount(request.getParameter("account"));
        if (user.checkPassword(request.getParameter("password"))) {
            final HttpSession session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return Pages.INDEX;
        }
        return Pages.UNAUTHORIZED;
    }

    public void save(User user) {
        try {
            userDao.insert(user);
        } catch (Exception e) {
            throw new RegisterFailException();
        }
    }
}
