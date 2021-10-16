package di.depdency.strategy;

import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodInjectStrategy implements InjectStrategy<Method> {

    @Override
    public boolean supports(Method type) {
        Class<?>[] parameterTypes = type.getParameterTypes();
        Set<Class<?>> distinctType = Arrays.stream(parameterTypes)
                .collect(Collectors.toSet());
        if (parameterTypes.length != distinctType.size()) {
            throw new ImpossibleDependencyException("메서드에 중복 필드가 있습니다.");
        }
        return true;
    }

    @Override
    public Set<Class<?>> findDependencies(Method type) {
        Class<?>[] parameterTypes = type.getParameterTypes();
        Set<Class<?>> dependencies = Arrays.stream(parameterTypes)
                .collect(Collectors.toSet());
        dependencies.add(type.getDeclaringClass());
        return dependencies;
    }

    @Override
    public void instantiate(Method type, ComponentContainer componentContainer) throws ReflectiveOperationException {
        Class<?> declaringClass = type.getDeclaringClass();
        Object declaredClassInstance = componentContainer.takeComponent(declaringClass);
        int parameterCount = type.getParameterCount();
        Class<?>[] parameterTypes = type.getParameterTypes();
        Object[] parameters = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            parameters[i] = componentContainer.takeComponent(parameterTypes[i]);
        }
        Object instance = type.invoke(declaredClassInstance, parameters);
        componentContainer.register(type.getReturnType(), instance);
    }
}
