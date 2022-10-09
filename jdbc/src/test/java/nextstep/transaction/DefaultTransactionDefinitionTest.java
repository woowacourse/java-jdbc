package nextstep.transaction;

import static nextstep.transaction.support.TransactionIsolation.ISOLATION_DEFAULT;
import static nextstep.transaction.support.TransactionPropagation.PROPAGATION_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class DefaultTransactionDefinitionTest {

    @Test
    void defaultTransactionDefinition() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        assertAll(
                () -> assertThat(defaultTransactionDefinition.getIsolationLevel()).isEqualTo(ISOLATION_DEFAULT),
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
