package com.interface21.jdbc.support.h2;

import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.BadGrammarException;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessResourceFailureException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.dao.DuplicateKeyException;

public class H2SQLExceptionTranslator {

    private final H2BadGrammarsErrorCodes badGrammarsErrorCodes;
    private final List<Integer> dataIntegrityViolationCodes;
    private final List<Integer> duplicateKeyErrorCodes;
    private final List<Integer> dataAccessResourceFailureCodes;
    private final List<Integer> cannotAcquireLockCodes;

    public H2SQLExceptionTranslator() {
        this.badGrammarsErrorCodes = new H2BadGrammarsErrorCodes();
        this.dataIntegrityViolationCodes = List.of(22001, 22003, 22012, 22018, 22025, 23000, 23002, 23003, 23502, 23503, 23506, 23507, 23513);
        this.duplicateKeyErrorCodes = List.of(23001, 23505);
        this.dataAccessResourceFailureCodes = List.of(90046, 90100, 90117, 90121, 90126);
        this.cannotAcquireLockCodes = List.of(50200);
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
