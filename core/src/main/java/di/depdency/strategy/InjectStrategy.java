package di.depdency.strategy;

import di.component.ComponentContainer;

import java.util.Set;

public interface InjectStrategy<T> {

    boolean supports(T type);

    Set<Class<?>> findDependencies(T type);

    void instantiate(T type, ComponentContainer componentContainer) throws ReflectiveOperationException;
}
