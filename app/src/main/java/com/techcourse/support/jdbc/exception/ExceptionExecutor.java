package com.techcourse.support.jdbc.exception;

import com.interface21.dao.DataAccessException;

public class ExceptionExecutor {

    private ExceptionExecutor() {
    }

    public static void run(RunnableWithException runnable) {
        run(runnable, () -> {
        }, () -> {
        });
    }

    public static void run(
            RunnableWithException runnable,
            Runnable runnableOnCatch,
            Runnable runnableOnFinally
    ) {
        try {
            runnable.run();
        } catch (Exception e) {
            runnableOnCatch.run();
            throw new DataAccessException(e);
        } finally {
            runnableOnFinally.run();
        }
    }
}
