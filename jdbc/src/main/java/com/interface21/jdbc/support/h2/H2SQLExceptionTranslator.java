package com.interface21.jdbc.support.h2;

import java.sql.SQLException;
import com.interface21.dao.BadGrammarException;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessResourceFailureException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.dao.DuplicateKeyException;

public class H2SQLExceptionTranslator {

    private final H2BadGrammarsErrorCodes badGrammarsErrorCodes;
    private final H2DataIntegrityViolationCodes dataIntegrityViolationCodes;
    private final H2DuplicateKeyErrorCodes duplicateKeyErrorCodes;
    private final H2DataAccessResourceFailureCodes dataAccessResourceFailureCodes;
    private final H2CanNotAcquireLockCodes cannotAcquireLockCodes;

    public H2SQLExceptionTranslator() {
        this.badGrammarsErrorCodes = new H2BadGrammarsErrorCodes();
        this.dataIntegrityViolationCodes = new H2DataIntegrityViolationCodes();
        this.duplicateKeyErrorCodes = new H2DuplicateKeyErrorCodes();
        this.dataAccessResourceFailureCodes = new H2DataAccessResourceFailureCodes();
        this.cannotAcquireLockCodes = new H2CanNotAcquireLockCodes();
    }

    public DataAccessException translate(SQLException sqlException) {
        if (badGrammarsErrorCodes.contains(sqlException.getErrorCode())) {
            return new BadGrammarException(sqlException);
        }

        if (duplicateKeyErrorCodes.contains(sqlException.getErrorCode())) {
            return new DuplicateKeyException(sqlException);
        }

        if (dataIntegrityViolationCodes.contains(sqlException.getErrorCode())) {
            return new DataIntegrityViolationException(sqlException);
        }

        if (dataAccessResourceFailureCodes.contains(sqlException.getErrorCode())) {
            return new DataAccessResourceFailureException(sqlException);
        }

        if (cannotAcquireLockCodes.contains(sqlException.getErrorCode())) {
            return new CannotAcquireLockException(sqlException);
        }

        return new DataAccessException(sqlException);
    }
}
