package di.depdency.strategy;

import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.getConstructors;
import static org.reflections.ReflectionUtils.withAnnotation;

public class ConstructorInjectStrategy extends AnnotationBasedClassInjectStrategy {

    public ConstructorInjectStrategy(Class<? extends Annotation> annotationClass) {
        super(annotationClass);
    }

    @Override
    public boolean supports(Class<?> type) {
        if (hasInjectConstructor(type)) {
            validateSingleInjectConstructor(type);
            return true;
        }
        return hasSingleConstructor(type);
    }

    private boolean hasInjectConstructor(Class<?> aClass) {
        return !findConstructorsWithAnnotation(aClass).isEmpty();
    }

    private Set<Constructor> findConstructorsWithAnnotation(Class<?> aClass) {
        return getConstructors(aClass, withAnnotation(annotationClass));
    }

    private void validateSingleInjectConstructor(Class<?> aClass) {
        Set<Constructor> constructors = findConstructorsWithAnnotation(aClass);
        if (constructors.size() != 1) {
            throw new ImpossibleDependencyException(
                    String.format("@%s가 붙어있는 생성자가 여러개 있습니다.", annotationClass.getSimpleName())
            );
        }
    }

    private boolean hasSingleConstructor(Class<?> aClass) {
        Constructor<?>[] constructors = aClass.getConstructors();
        if (constructors.length != 1) {
            throw new ImpossibleDependencyException(
                    String.format("생성자는 하나만 존재해야합니다.", annotationClass.getSimpleName())
            );
        }
        return true;
    }

    @Override
    public Set<Class<?>> findDependencies(Class<?> type) {
        Constructor constructor = takeProperConstructor(type);
        return extractParameterTypes(constructor);
    }

    private Constructor takeProperConstructor(Class<?> type) {
        if (hasInjectConstructor(type)) {
            return extractSingleConstructor(
                    findConstructorsWithAnnotation(type)
            );
        } else if (hasSingleConstructor(type)) {
            return extractSingleConstructor(
                    findAllConstructors(type)
            );
        }
        return null;
    }

    private Constructor extractSingleConstructor(Set<Constructor> constructors) {
        return constructors.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("생성자가 없어 추출할 수 없습니다."));
    }

    private Set<Constructor> findAllConstructors(Class<?> aClass) {
        return getConstructors(aClass, constructor -> true);
    }

    private Set<Class<?>> extractParameterTypes(Constructor constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        validateParameters(parameterTypes);

        return Arrays.stream(parameterTypes)
                .collect(Collectors.toSet());
    }

    private void validateParameters(Class<?>[] parameterTypes) {
        Set<Class<?>> distinctTypes = Arrays.stream(parameterTypes)
                .collect(Collectors.toSet());
        if (parameterTypes.length != distinctTypes.size()) {
            throw new ImpossibleDependencyException("생성자의 파라미터에 타입 중복이 있습니다.");
        }
    }

    @Override
    public void instantiate(Class<?> aClass, ComponentContainer componentContainer) throws ReflectiveOperationException {
        Constructor constructor = takeProperConstructor(aClass);
        Class[] parameterTypes = constructor.getParameterTypes();
        int parameterCount = parameterTypes.length;
        Object[] parameters = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            Class parameterType = parameterTypes[i];
            parameters[i] = componentContainer.takeComponent(parameterType);
        }
        componentContainer.register(aClass, constructor.newInstance(parameters));
    }
}
