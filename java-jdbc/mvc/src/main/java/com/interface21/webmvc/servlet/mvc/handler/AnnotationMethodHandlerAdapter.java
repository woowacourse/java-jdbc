package com.interface21.webmvc.servlet.mvc.handler;

import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecution;
import com.interface21.webmvc.servlet.view.ModelAndView;
import com.interface21.webmvc.servlet.view.View;
import com.interface21.webmvc.servlet.view.ViewResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AnnotationMethodHandlerAdapter implements HandlerAdapter {

    private final ViewResolver viewResolver;

    public AnnotationMethodHandlerAdapter(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    @Override
    public ModelAndView handle(Object handler, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HandlerExecution handlerExecution = (HandlerExecution) handler;
        Object returnType = handlerExecution.handle(request, response);

        if (returnType instanceof ModelAndView) {
            return (ModelAndView) returnType;
        }
        if (returnType instanceof String) {
            View view = viewResolver.resolveViewName((String) returnType);
            return new ModelAndView(view);
        }
        if (returnType instanceof View) {
            return new ModelAndView((View) returnType);
        }
        throw new IllegalArgumentException("실행할 핸들러가 존재하지 않습니다. : " + handler.getClass().getName());
    }
}
