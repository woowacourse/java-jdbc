package com.techcourse.controller;

import ch.qos.logback.core.model.Model;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.view.JsonView;
import com.interface21.webmvc.servlet.view.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String execute(final HttpServletRequest request, final HttpServletResponse response) {
        return "redirect:/index";
    }

    @RequestMapping(value = "/api/data", method = RequestMethod.GET)
    public ModelAndView getData(final HttpServletRequest request, final HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView(new JsonView());
        modelAndView.addObject("message", "Hello, JSON World!");
        modelAndView.addObject("timestamp", System.currentTimeMillis());
        return modelAndView;
    }
}
