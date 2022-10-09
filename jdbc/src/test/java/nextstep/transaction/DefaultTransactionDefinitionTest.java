package nextstep.transaction;

import static nextstep.transaction.support.TransactionIsolation.READ_UNCOMMITTED;
import static nextstep.transaction.support.TransactionPropagation.PROPAGATION_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class DefaultTransactionDefinitionTest {

    @Test
    void defaultTransactionDefinition() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        assertAll(
                () -> assertThat(defaultTransactionDefinition.getIsolationLevel()).isEqualTo(READ_UNCOMMITTED),
                () -> assertThat(defaultTransactionDefinition.getPropagationBehavior()).isEqualTo(PROPAGATION_REQUIRED),
                () -> assertThat(defaultTransactionDefinition.isReadOnly()).isFalse()
        );
    }

    @Test
    void setReadOnly() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setReadOnly(true);

        assertThat(defaultTransactionDefinition.isReadOnly()).isTrue();
    }
}
