package org.springframework.dao;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SQLExceptionTranslatorTest {

    @Test
    void translateException() {
        Assertions.assertThatThrownBy(this::doThrow)
                .isInstanceOf(org.springframework.dao.SQLTimeoutException.class);
    }

    private void doThrow() {
        try {
            throw new SQLTimeoutException();
        } catch (SQLException e) {
            throw SQLExceptionTranslator.translate(e);
        }
    }
}
