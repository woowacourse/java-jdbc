package com.interface21.jdbc.support.h2;

import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.ErrorCodes;

public class H2SQLExceptionTranslator {

    private final List<ErrorCodes> errorCodes;

    public H2SQLExceptionTranslator() {
        this.errorCodes = List.of(
                new H2BadGrammarsErrorCodes(),
                new H2DataIntegrityViolationCodes(),
                new H2DuplicateKeyErrorCodes(),
                new H2DataAccessResourceFailureCodes(),
                new H2CanNotAcquireLockCodes()
        );
    }

    public DataAccessException translate(SQLException sqlException) {
        return errorCodes.stream()
                .filter(it -> it.contains(sqlException.getErrorCode()))
                .findFirst()
                .map(it -> it.translate(sqlException))
                .orElse(new DataAccessException(sqlException));
    }
}
