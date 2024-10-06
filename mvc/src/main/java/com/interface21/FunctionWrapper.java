package com.interface21;

import java.util.function.Function;

public class FunctionWrapper {

    public static <T, R> Function<T, R> apply(ThrowingFunction<T, R, Exception> function) {
        return i -> {
            try {
                return function.apply(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private FunctionWrapper() {}
}
