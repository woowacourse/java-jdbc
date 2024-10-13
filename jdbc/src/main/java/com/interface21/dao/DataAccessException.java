package com.interface21.dao;

import java.util.concurrent.Callable;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static <T> T executeAndConvertException(Callable<T> callable) {
        try {
            return callable.call();
        }catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public static void executeAndConvertException(RunAndThrowable runAndThrowable) {
        try {
            runAndThrowable.run();
        }catch (Throwable t) {
            throw new DataAccessException(t);
        }
    }

    public DataAccessException() {
        super();
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    @FunctionalInterface
    public interface RunAndThrowable {
        void run() throws Throwable;
    }
}
