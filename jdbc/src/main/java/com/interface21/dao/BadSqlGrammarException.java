package com.interface21.dao;

public class BadSqlGrammarException extends DataAccessException {

    public BadSqlGrammarException() {
        super("You have an error in your SQL syntax");
    }
}
