package di.component;


import di.annotation.Component;
import di.annotation.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.ReflectionUtils.getMethods;

public class ComponentScanner {

    private final Reflections reflections;

    public ComponentScanner(String... packagePaths) {
        ClassLoader classLoader = ClasspathHelper.contextClassLoader();
        Collection<URL> urls = new ArrayList<>();
        for (String path : packagePaths) {
            urls.addAll(ClasspathHelper.forPackage(path, classLoader));
        }
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setUrls(urls)
                .addClassLoader(classLoader)
                .setScanners(
                        new ResourcesScanner(),
                        new FieldAnnotationsScanner(),
                        new TypeAnnotationsScanner(),
                        new SubTypesScanner());

        reflections = new Reflections(configurationBuilder);
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

    public Set<Method> scanComponentMethodsFromConfiguration() {
        return scanComponentClasses().stream()
                .filter(aClass -> aClass.isAnnotationPresent(Configuration.class))
                .flatMap(this::takeMethodsWithComponent)
                .collect(Collectors.toSet());
    }

    private Stream<Method> takeMethodsWithComponent(Class<?> aClass) {
        Set<Method> methods = getMethods(aClass, method -> method.isAnnotationPresent(Component.class));
        return methods.stream();
    }
}
