package di.component;


import di.annotation.Component;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentScanner {

    private final Reflections reflections;

    public ComponentScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Set<Class<?>> scanComponentClasses() {
        return scanAnnotationWithComponent()
                .stream()
                .map(annotation -> (Class<Annotation>) annotation)
                .flatMap(this::scanClassesWithAnnotation)
                .collect(Collectors.toSet());
    }

    private Set<Class<?>> scanAnnotationWithComponent() {
        Reflections reflectionsInAnnotationPackage = new Reflections("di.annotation");
        Set<Class<?>> annotations = reflectionsInAnnotationPackage
                .getTypesAnnotatedWith(Component.class)
                .stream()
                .filter(Class::isAnnotation)
                .collect(Collectors.toSet());

        annotations.add(Component.class);
        return annotations;
    }

    private Stream<Class<?>> scanClassesWithAnnotation(Class<Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation)
                .stream()
                .filter(type -> !type.isInterface() && !type.isAnnotation());
    }
}
