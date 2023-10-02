package org.springframework.jdbc.core;

public class StatementAgentServiceFailedException extends RuntimeException {

    public StatementAgentServiceFailedException(Throwable e) {
        super(e);
    }
}
