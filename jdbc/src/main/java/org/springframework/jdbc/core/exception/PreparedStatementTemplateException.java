package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class PreparedStatementTemplateException extends DataAccessException {

    public PreparedStatementTemplateException(final Throwable cause) {
        super(cause);
    }
}
