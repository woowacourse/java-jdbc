package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.interface21.dao.DuplicateKeyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.interface21.webmvc.servlet.view.JspView;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;

@Controller
public class RegisterController {

    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final var user = new User(
                    request.getParameter("account"),
                    request.getParameter("password"),
                    request.getParameter("email"));
            userDao.insert(user);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ModelAndView(new JspView("/register.jsp"))
                    .addObject("errorMessage", e.getMessage());
        } catch (DuplicateKeyException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return new ModelAndView(new JspView("/register.jsp"))
                    .addObject("errorMessage", "이미 사용 중인 계정입니다.");
        }

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
