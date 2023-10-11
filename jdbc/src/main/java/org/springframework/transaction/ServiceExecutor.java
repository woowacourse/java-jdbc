package org.springframework.transaction;

@FunctionalInterface
public interface ServiceExecutor {

    void doInAction();
}
