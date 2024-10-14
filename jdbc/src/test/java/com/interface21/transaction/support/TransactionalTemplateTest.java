package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionalTemplateTest {

    Transaction transaction = Mockito.mock(Transaction.class);
    TransactionalTemplate transactionalTemplate = new TransactionalTemplate();

    @Test
    void throw_exception_with_rollback() throws SQLException {
        assertThatThrownBy(() -> transactionalTemplate.execute(transaction, transaction -> {
            throw new SQLException("");
        })).isInstanceOf(TransactionalException.class);

        Mockito.verify(transaction, Mockito.times(1))
                .begin();
        Mockito.verify(transaction, Mockito.times(1))
                .rollback();
        Mockito.verify(transaction, Mockito.times(1))
                .close();
    }

    @Test
    void execute_with_transaction() throws SQLException {
        transactionalTemplate.execute(transaction, nx -> {});

        Mockito.verify(transaction, Mockito.times(1))
                .begin();
        Mockito.verify(transaction, Mockito.times(1))
                .commit();
        Mockito.verify(transaction, Mockito.times(1))
                .close();
    }

}
