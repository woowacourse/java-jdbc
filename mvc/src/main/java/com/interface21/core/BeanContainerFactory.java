package com.interface21.core;

public class BeanContainerFactory {

    private BeanContainerFactory() {
    }

    public static BeanContainer getContainer() {
        return SingletonBeanContainer.getInstance();
    }
}
