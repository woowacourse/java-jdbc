package org.springframework.transaction;

@FunctionalInterface
public interface ServiceCallback<T> {

    T doInAction();
}
