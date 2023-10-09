package org.springframework.jdbc.core;

public interface TransactionalOperation {

    void run(Transaction transaction);
}
