package com.interface21.core;

import java.lang.annotation.Annotation;
import java.util.List;

public interface BeanContainer {

    Object registerBean(Class<?> clazz);

    Object getBean(Class<?> clazz);

    List<Object> getAnnotatedBeans(Class<? extends Annotation> annotation);
}
