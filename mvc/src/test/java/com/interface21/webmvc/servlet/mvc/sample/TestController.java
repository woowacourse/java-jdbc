package com.interface21.webmvc.servlet.mvc.sample;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JspView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Controller
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @RequestMapping(value = "/get-test", method = RequestMethod.GET)
    public ModelAndView findUserId(final HttpServletRequest request, final HttpServletResponse response) {
        log.info("test controller get method");
        final var modelAndView = new ModelAndView(new JspView(""));
        modelAndView.addObject("id", request.getAttribute("id"));
        return modelAndView;
    }

    @RequestMapping(value = "/post-test", method = RequestMethod.POST)
    public ModelAndView save(final HttpServletRequest request, final HttpServletResponse response) {
        log.info("test controller post method");
        final var modelAndView = new ModelAndView(new JspView(""));
        modelAndView.addObject("id", request.getAttribute("id"));
        return modelAndView;
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
        log.info("test controller test method");
        ModelAndView modelAndView = mock(ModelAndView.class);
        when(modelAndView.getObject("test")).thenReturn("test");
        return modelAndView;
    }

    @RequestMapping(value = "/notExistMethod")
    public ModelAndView notExistMethod(HttpServletRequest request, HttpServletResponse response) {
        log.info("test controller test method");
        ModelAndView modelAndView = mock(ModelAndView.class);
        when(modelAndView.getObject("notExistMethod")).thenReturn("notExistMethod");
        return modelAndView;
    }
}
