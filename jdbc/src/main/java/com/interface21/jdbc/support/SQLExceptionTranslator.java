package com.interface21.jdbc.support;

import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.BadGrammarException;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessResourceFailureException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.dao.DuplicateKeyException;

class SQLExceptionTranslator {

    public DataAccessException translate(SQLException sqlException) {
        List<Integer> badGrammarsErrorCodes = List.of(42000, 42001, 42101, 42102, 42111, 42112, 42121, 42122, 42132);
        List<Integer> duplicateKeyErrorCodes = List.of(23001, 23505);
        List<Integer> dataIntegrityViolationCodes = List.of(22001, 22003, 22012, 22018, 22025, 23000, 23002, 23003, 23502, 23503, 23506, 23507, 23513);
        List<Integer> dataAccessResourceFailureCodes = List.of(90046, 90100, 90117, 90121, 90126);
        List<Integer> cannotAcquireLockCodes = List.of(50200);

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
