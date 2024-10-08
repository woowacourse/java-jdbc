package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.ReturnValueHandler;
import java.util.List;

public class ReturnValueHandlers {

    private final List<ReturnValueHandler> returnValueHandlers;

    public ReturnValueHandlers() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        this.returnValueHandlers = beanContainer.getSubTypeBeansOf(ReturnValueHandler.class);
    }

    public ModelAndView handle(Object returnValue) {
        ReturnValueHandler handler = returnValueHandlers.stream()
                .filter(returnValueHandler -> returnValueHandler.supports(returnValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("처리할 수 없는 반환값입니다."));

        return handler.handle(returnValue);
    }
}
