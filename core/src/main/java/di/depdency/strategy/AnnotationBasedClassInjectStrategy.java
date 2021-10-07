package di.depdency.strategy;

import di.component.ComponentContainer;

import java.lang.annotation.Annotation;

public abstract class AnnotationBasedClassInjectStrategy implements InjectStrategy<Class<?>> {

    protected final Class<? extends Annotation> annotationClass;

    protected AnnotationBasedClassInjectStrategy(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public abstract void instantiate(Class<?> aClass, ComponentContainer componentContainer) throws ReflectiveOperationException;
}
