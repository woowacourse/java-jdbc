package com.interface21.bean.scanner;

import java.util.Set;
import org.reflections.Reflections;

public class SubTypeScanner {

    private SubTypeScanner() {
    }

    public static <T> Set<Class<? extends T>> subTypeScan(Class<T> parentsClass, String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(parentsClass);
    }
}
