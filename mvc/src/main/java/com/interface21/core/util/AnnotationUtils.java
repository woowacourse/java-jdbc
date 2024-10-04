package com.interface21.core.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    private static final Map<Class<? extends Annotation>, Set<Class<? extends Annotation>>> annotationCaches = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Annotation>, Set<Class<? extends Annotation>>> visitedAnnotations = new ConcurrentHashMap<>();

    public static boolean hasMetaAnnotatedClasses(Class<?> clazz,
                                                  Class<? extends Annotation> targetAnnotation) {
        annotationCaches.putIfAbsent(targetAnnotation, ConcurrentHashMap.newKeySet());
        visitedAnnotations.putIfAbsent(targetAnnotation, ConcurrentHashMap.newKeySet());

        return Arrays.stream(clazz.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .map(annotation -> hasMetaAnnotatedClasses(
                        annotation,
                        targetAnnotation,
                        visitedAnnotations.get(targetAnnotation))
                ).anyMatch(Predicate.isEqual(true));
    }

    // Depth-first search to find meta-annotated classes recursively
    private static boolean hasMetaAnnotatedClasses(Class<? extends Annotation> currentAnnotation,
                                                   Class<? extends Annotation> targetAnnotation,
                                                   Set<Class<? extends Annotation>> visited) {
        visited.add(currentAnnotation);
        Set<Class<? extends Annotation>> cache = annotationCaches.get(targetAnnotation);
        if (cache.contains(currentAnnotation) || currentAnnotation.equals(targetAnnotation)) {
            return true;
        }
        boolean result = Arrays.stream(currentAnnotation.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .filter(annotation -> !visited.contains(annotation) || annotation.equals(targetAnnotation))
                .map(annotation -> hasMetaAnnotatedClasses(annotation, targetAnnotation, visited)) // Recursive call
                .anyMatch(Predicate.isEqual(true));
        if (result) {
            cache.add(currentAnnotation);
        }
        return result;
    }
}
