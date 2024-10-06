package com.interface21.webmvc.servlet.mvc;

import com.interface21.BeanContainer;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class HandlerMappings {

    private final List<HandlerMapping> handlerMappings;

    public HandlerMappings() {
        this.handlerMappings = new ArrayList<>();
    }

    public void initialize() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        List<HandlerMapping> mappings = beanContainer.getBeans(HandlerMapping.class);
        mappings.forEach(HandlerMapping::initialize);
        handlerMappings.addAll(mappings);
    }

    public Object getHandler(HttpServletRequest request) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                return handlerMapping.getHandler(request);
            } catch (IllegalArgumentException e) {}
        }
        throw new IllegalArgumentException("일치하는 handler가 없습니다");
    }
}
