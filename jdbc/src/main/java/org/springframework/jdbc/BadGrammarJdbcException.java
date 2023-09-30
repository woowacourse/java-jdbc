package org.springframework.jdbc;

import java.sql.SQLException;

public class BadGrammarJdbcException extends RuntimeException {
    public BadGrammarJdbcException(String msg) {
        super(msg);
    }

    public BadGrammarJdbcException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
