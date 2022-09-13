package nextstep.mvc;

import nextstep.mvc.view.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
    boolean supports(final Object handler);

    ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception;
}
