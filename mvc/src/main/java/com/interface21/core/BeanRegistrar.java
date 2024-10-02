package com.interface21.core;

import com.interface21.context.stereotype.Component;
import com.interface21.core.util.AnnotationUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class BeanRegistrar {

    private BeanRegistrar() {
    }

    public static void registerBeans(Class<?> basePackageClass) {
        BeanContainer container = BeanContainerFactory.getContainer();
        Scanners scanners = Scanners.SubTypes.filterResultsBy(filter -> true);
        Reflections reflections = new Reflections(basePackageClass.getPackageName(), scanners);

        reflections.getSubTypesOf(Object.class)
                .stream()
                .filter(clazz -> AnnotationUtils.hasMetaAnnotatedClasses(clazz, Component.class))
                .forEach(container::registerBean);
    }
}
