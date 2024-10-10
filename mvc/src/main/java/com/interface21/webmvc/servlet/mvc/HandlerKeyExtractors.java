package com.interface21.webmvc.servlet.mvc;

import com.interface21.BeanContainer;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerKey;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HandlerKeyExtractors {

    private final List<HandlerKeyExtractor> handlerKeyExtractors;

    public HandlerKeyExtractors() {
        this.handlerKeyExtractors = new ArrayList<>();
    }

    public void initialize() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        List<HandlerKeyExtractor> keyExtractors = beanContainer.getBeans(HandlerKeyExtractor.class);
        handlerKeyExtractors.addAll(keyExtractors);
    }

    public List<HandlerKey> extract(Method method) {
        HandlerKeyExtractor handlerKeyExtractor = handlerKeyExtractors.stream()
                .filter(keyExtractor -> keyExtractor.supports(method))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("HandlerKey를 추출할 수 없습니다"));
        return handlerKeyExtractor.extract(method);
    }
}
