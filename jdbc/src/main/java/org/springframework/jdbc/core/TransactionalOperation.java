package org.springframework.jdbc.core;

public interface TransactionalOperation {

    void execute() throws Exception;
}
