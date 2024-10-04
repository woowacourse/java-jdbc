package com.interface21.core;

public class BeanNotFoundException extends RuntimeException {

    public BeanNotFoundException(Class<?> clazz) {
        super("Bean not found for [" + clazz + "]");
    }
}
