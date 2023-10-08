package org.springframework.jdbc.support;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicationKeyException;
import org.springframework.jdbc.exception.BadSqlGrammarException;

public class H2SQLErrorCodeToDataAccessExceptionMapper {

    private static final Map<Integer, Class<? extends DataAccessException>> mapper = new HashMap<>();

    static {
        setUpBadSqlGrammarCodes();
        setUpDuplicationKeyException();
        setUpDataIntegrityViolationException();
        setUpDataAccessResourceFailureException();
        setUpCannotAcquireLockException();
    }

    private static void setUpBadSqlGrammarCodes() {
        mapper.put(42000, BadSqlGrammarException.class);
        mapper.put(42001, BadSqlGrammarException.class);
        mapper.put(42101, BadSqlGrammarException.class);
        mapper.put(42102, BadSqlGrammarException.class);
        mapper.put(42111, BadSqlGrammarException.class);
        mapper.put(42112, BadSqlGrammarException.class);
        mapper.put(42121, BadSqlGrammarException.class);
        mapper.put(42122, BadSqlGrammarException.class);
        mapper.put(42132, BadSqlGrammarException.class);
    }

    private static void setUpDuplicationKeyException() {
        mapper.put(23001, DuplicationKeyException.class);
        mapper.put(23505, DuplicationKeyException.class);
    }

    private static void setUpDataIntegrityViolationException() {
        mapper.put(22001, DataIntegrityViolationException.class);
        mapper.put(22003, DataIntegrityViolationException.class);
        mapper.put(22012, DataIntegrityViolationException.class);
        mapper.put(22018, DataIntegrityViolationException.class);
        mapper.put(22025, DataIntegrityViolationException.class);
        mapper.put(23000, DataIntegrityViolationException.class);
        mapper.put(23002, DataIntegrityViolationException.class);
        mapper.put(23003, DataIntegrityViolationException.class);
        mapper.put(23502, DataIntegrityViolationException.class);
        mapper.put(23503, DataIntegrityViolationException.class);
        mapper.put(23506, DataIntegrityViolationException.class);
        mapper.put(23507, DataIntegrityViolationException.class);
        mapper.put(23513, DataIntegrityViolationException.class);
    }

    private static void setUpDataAccessResourceFailureException() {
        mapper.put(90046, DataAccessResourceFailureException.class);
        mapper.put(90100, DataAccessResourceFailureException.class);
        mapper.put(90117, DataAccessResourceFailureException.class);
        mapper.put(90121, DataAccessResourceFailureException.class);
        mapper.put(90126, DataAccessResourceFailureException.class);
    }

    private static void setUpCannotAcquireLockException() {
        mapper.put(50200, CannotAcquireLockException.class);
    }

    public static Class<? extends DataAccessException> mapSQLErrorCode(int errorCode) {
        return mapper.get(errorCode);
    }
}
