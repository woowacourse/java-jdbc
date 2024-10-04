package com.interface21.bean.scanner;

import com.interface21.context.stereotype.Component;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;

public class ComponentScanner {

    private static final Map<Annotation, Boolean> hasComponentCache = new ConcurrentHashMap<>();

    private ComponentScanner() {
    }

    public static List<Class<?>> componentScan(String packageName) {
        Scanner scanner = Scanners.SubTypes.filterResultsBy(c -> true);
        Reflections reflections = new Reflections(packageName, scanner);

        return reflections.getSubTypesOf(Object.class).stream()
                .filter(ComponentScanner::hasComponentAnnotation)
                .toList();
    }

    private static boolean hasComponentAnnotation(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        Set<Annotation> visited = new HashSet<>();

        return Arrays.stream(annotations)
                .anyMatch(annotation -> hasComponent(annotation, visited));
    }

    private static boolean hasComponent(Annotation annotation, Set<Annotation> visited) {
        if (hasComponentCache.containsKey(annotation)) {
            return hasComponentCache.get(annotation);
        }
        if (annotation instanceof Component) {
            hasComponentCache.put(annotation, true);
            return true;
        }
        Annotation[] metaAnnotations = annotation.annotationType().getAnnotations();

        for (Annotation metaAnnotation : metaAnnotations) {
            if (visited.contains(metaAnnotation)) {
                continue;
            }
            visited.add(metaAnnotation);
            if (hasComponent(metaAnnotation, visited)) {
                hasComponentCache.put(annotation, true);
                return true;
            }
        }
        hasComponentCache.put(annotation, false);
        return false;
    }
}
