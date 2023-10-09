package org.springframework.transaction.support;

@FunctionalInterface
public interface ServiceCallback {
    void doGetTransaction();
}
