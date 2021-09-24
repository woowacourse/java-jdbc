package com.techcourse.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

@Controller
public class BaseController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String base(HttpServletRequest req, HttpServletResponse res) {
        return "/index.jsp";
    }
}
