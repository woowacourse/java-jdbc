package org.springframework.jdbc.core.exception;

import org.springframework.dao.DataAccessException;

public class ResultSetTemplateException extends DataAccessException {

    public ResultSetTemplateException(final Throwable cause) {
        super(cause);
    }
}
