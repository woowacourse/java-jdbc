package com.interface21.webmvc.servlet.mvc;

import com.interface21.webmvc.servlet.mvc.tobe.HandlerKey;
import java.lang.reflect.Method;
import java.util.List;

public interface HandlerKeyExtractor {

    boolean supports(Method method);

    List<HandlerKey> extract(Method method);
}
