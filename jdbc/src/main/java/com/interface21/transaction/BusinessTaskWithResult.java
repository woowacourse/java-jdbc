package com.interface21.transaction;

@FunctionalInterface
public interface BusinessTaskWithResult<T> {

    T runBusinessLogic();
}
