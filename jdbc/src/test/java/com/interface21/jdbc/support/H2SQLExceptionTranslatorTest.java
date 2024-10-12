package com.interface21.jdbc.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import com.interface21.dao.BadGrammarException;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessResourceFailureException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.dao.DuplicateKeyException;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class H2SQLExceptionTranslatorTest {

    @ParameterizedTest
    @ValueSource(ints = {42000, 42001, 42101, 42102, 42111, 42112, 42121, 42122, 42132})
    @DisplayName("SQL Exception을 추상화된 예외로 변경한다. - h2 문법 오류")
    void translateBadGrammar(int errorCode) {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(errorCode);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(BadGrammarException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {23001, 23505})
    @DisplayName("SQL Exception을 추상화된 예외로 변경한다. - h2 중복키 오류")
    void translateDuplicatedKey(int errorCode) {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(errorCode);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(DuplicateKeyException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {22001, 22003, 22012, 22018, 22025, 23000, 23002, 23003, 23502, 23503, 23506, 23507, 23513})
    @DisplayName("SQL Exception을 추상화된 예외로 변경한다. - h2 제약조건 오류")
    void translateDataIntegrityViolation(int errorCode) {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(errorCode);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {90046, 90100, 90117, 90121, 90126})
    @DisplayName("SQL Exception을 추상화된 예외로 변경한다. - h2 데이터 접근 오류")
    void translateDataAccessResourceFailureCodes(int errorCode) {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(errorCode);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    @DisplayName("SQL Exception을 추상화된 예외로 변경한다. - h2 락 획득 실패 오류")
    void translateDataAccessResourceFailureCodes() {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(50200);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(CannotAcquireLockException.class);
    }

    @Test
    @DisplayName("모르는 코드가 들어오면 DataAccessException으로 변경한다.")
    void unknownCode() {
        H2SQLExceptionTranslator h2SqlExceptionTranslator = new H2SQLExceptionTranslator();
        SQLException sqlException = Mockito.mock(SQLException.class);
        when(sqlException.getErrorCode())
                .thenReturn(-1);

        DataAccessException dae = h2SqlExceptionTranslator.translate(sqlException);

        assertThat(dae).isInstanceOf(DataAccessException.class);
    }
}
