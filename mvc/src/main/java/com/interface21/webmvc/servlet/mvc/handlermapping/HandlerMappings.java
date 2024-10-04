package com.interface21.webmvc.servlet.mvc.handlermapping;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerMappings {

    private static final Logger log = LoggerFactory.getLogger(HandlerMappings.class);

    private final List<HandlerMapping> handlerMappings;

    public HandlerMappings() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        this.handlerMappings = beanContainer.getSubTypeBeansOf(HandlerMapping.class);
    }

    public void initialize() {
        handlerMappings.forEach(HandlerMapping::initialize);
    }

    public Optional<Object> findHandler(HttpServletRequest request) {
        return handlerMappings.stream()
                .map(handlerMapping -> handlerMapping.getHandler(request))
                .filter(Objects::nonNull)
                .findFirst();
    }
}
