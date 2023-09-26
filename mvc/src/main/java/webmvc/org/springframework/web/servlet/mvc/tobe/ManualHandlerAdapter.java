package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.view.JspView;

public class ManualHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean isSupport(final Handler handler) {
        return handler instanceof MannualHandler;
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Handler handler) throws Exception {
        final Object result = handler.handle(request, response);
        if (result instanceof String) {
            return new ModelAndView(new JspView((String) result));
        }
        return null;
    }
}
