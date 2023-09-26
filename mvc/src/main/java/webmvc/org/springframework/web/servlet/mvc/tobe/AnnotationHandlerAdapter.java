package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.view.JspView;

public class AnnotationHandlerAdapter implements HandlerAdapter {
    @Override
    public boolean isSupport(Handler handler) {
        return handler instanceof AnnotationHandler;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        final Object result = handler.handle(request, response);
        if (result instanceof String) {
            return new ModelAndView(new JspView((String) result));
        }
        if (result instanceof ModelAndView) {
            return (ModelAndView) result;
        }
        return null;
    }
}
