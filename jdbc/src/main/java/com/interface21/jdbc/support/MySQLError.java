package com.interface21.jdbc.support;

import com.interface21.dao.BadSqlGrammarException;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.dao.DuplicateKeyException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

public enum MySQLError {
    BAD_SQL_GRAMMAR_EXCEPTION(Set.of(1064, 42001), BadSqlGrammarException::new),
    DUPLICATE_KEY_EXCEPTION(Set.of(1062, 23505), DuplicateKeyException::new),
    DATA_INTEGRITY_VIOLATION_EXCEPTION(Set.of(1451, 1452), DataIntegrityViolationException::new),
    DATA_ACCESS_EXCEPTION(Set.of(), DataAccessException::new),
    ;

    private final Set<Integer> code;
    private final Supplier<DataAccessException> exceptionSupplier;

    MySQLError(Set<Integer> code, Supplier<DataAccessException> exceptionSupplier) {
        this.code = code;
        this.exceptionSupplier = exceptionSupplier;
    }

    public static MySQLError from(int code) {
        return Arrays.stream(values())
                .filter(error -> error.code.contains(code))
                .findAny()
                .orElse(DATA_ACCESS_EXCEPTION);
    }

    public DataAccessException generateException() {
        return exceptionSupplier.get();
    }
}
