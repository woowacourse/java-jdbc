package di.depdency.strategy;

import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.getConstructors;
import static org.reflections.ReflectionUtils.getFields;
import static org.reflections.ReflectionUtils.withAnnotation;

public class FieldInjectStrategy extends AnnotationBasedClassInjectStrategy {

    public FieldInjectStrategy(Class<? extends Annotation> annotationClass) {
        super(annotationClass);
    }

    @Override
    public boolean supports(Class<?> type) {
        Set<Field> fields = findFieldsWithAnnotation(type);
        Set<Class<?>> classTypes = extractClassTypeByFields(fields);

        if (fields.isEmpty()) {
            return false;
        }
        if (fields.size() != classTypes.size()) {
            throw new ImpossibleDependencyException(
                    String.format("@%s가 붙어있는 중복 필드가 있습니다.", annotationClass.getSimpleName())
            );
        }
        if (takeDefaultConstructor(type).isEmpty()) {
            throw new ImpossibleDependencyException("필드 주입 전략은 기본 생성자가 필요합니다.");
        }
        return true;
    }

    private Set<Field> findFieldsWithAnnotation(Class<?> aClass) {
        return getFields(aClass, withAnnotation(annotationClass));
    }

    private Set<Class<?>> extractClassTypeByFields(Set<Field> fields) {
        return fields.stream()
                .map(Field::getType)
                .collect(Collectors.toSet());
    }

    private Optional<Constructor> takeDefaultConstructor(Class<?> aClass) {
        Set<Constructor> constructors = getConstructors(aClass, constructor -> constructor.getParameterCount() == 0);
        return constructors.stream()
                .findAny();
    }

    @Override
    public Set<Class<?>> findDependencies(Class<?> type) {
        Set<Field> fields = findFieldsWithAnnotation(type);
        return extractClassTypeByFields(fields);
    }

    @Override
    public void instantiate(Class<?> aClass, ComponentContainer componentContainer) throws ReflectiveOperationException {
        Constructor constructor = takeDefaultConstructor(aClass)
                .orElseThrow(() -> new IllegalStateException("기본 생성자가 없습니다."));
        Object instance = constructor.newInstance();
        Set<Field> fieldsWithAnnotation = findFieldsWithAnnotation(aClass);
        for (Field field : fieldsWithAnnotation) {
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            field.set(instance, componentContainer.takeComponent(fieldType));
        }
        componentContainer.register(aClass, instance);
    }
}
