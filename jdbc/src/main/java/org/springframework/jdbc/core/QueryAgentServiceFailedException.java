package org.springframework.jdbc.core;

public class QueryAgentServiceFailedException extends RuntimeException {

    public QueryAgentServiceFailedException(Throwable e) {
        super(e);
    }
}
