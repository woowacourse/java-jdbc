package org.springframework.jdbc.exception;

import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class BadSqlGrammarException extends DataAccessException {

    public BadSqlGrammarException(String msg) {
        super(msg);
    }

    public BadSqlGrammarException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
