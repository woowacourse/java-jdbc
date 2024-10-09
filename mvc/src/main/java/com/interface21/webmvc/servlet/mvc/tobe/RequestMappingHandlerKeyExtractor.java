package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.context.stereotype.Component;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.HandlerKeyExtractor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class RequestMappingHandlerKeyExtractor implements HandlerKeyExtractor {

    @Override
    public boolean supports(Method method) {
        return method.isAnnotationPresent(RequestMapping.class);
    }

    public List<HandlerKey> extract(Method method) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        String value = requestMapping.value();
        RequestMethod[] requestMethods = requestMapping.method();
        if (requestMethods.length == 0) {
            requestMethods = RequestMethod.values();
        }
        return Arrays.stream(requestMethods)
                .map(requestMethod -> new HandlerKey(value, requestMethod))
                .toList();
    }
}
