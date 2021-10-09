package di.depdency.strategy;

import di.component.ComponentContainer;
import exception.CoreException;

import java.lang.annotation.Annotation;

public abstract class AnnotationBasedClassInjectStrategy implements InjectStrategy<Class<?>> {

    protected final Class<? extends Annotation> annotationClass;

    protected AnnotationBasedClassInjectStrategy(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void registerInstance(Class<?> componentType, ComponentContainer componentContainer) {
        try {
            instantiate(componentType, componentContainer);
        } catch (ReflectiveOperationException e) {
            throw new CoreException("컴포넌트를 생성하는중 문제가 발생했습니다.");
        }
    }
}
