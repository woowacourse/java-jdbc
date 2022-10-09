package nextstep.transaction;

import nextstep.transaction.support.TransactionIsolation;
import nextstep.transaction.support.TransactionPropagation;

public class DefaultTransactionDefinition implements TransactionDefinition {

    private TransactionPropagation propagationBehavior;
    private TransactionIsolation isolationLevel;
    private boolean readOnly;

    public DefaultTransactionDefinition(final TransactionPropagation propagationBehavior,
                                        final TransactionIsolation isolationLevel,
                                        final boolean readOnly) {
        this.propagationBehavior = propagationBehavior;
        this.isolationLevel = isolationLevel;
        this.readOnly = readOnly;
    }

    public DefaultTransactionDefinition() {
        this(TransactionPropagation.PROPAGATION_REQUIRED, TransactionIsolation.READ_UNCOMMITTED, false);
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public TransactionPropagation getPropagationBehavior() {
        return propagationBehavior;
    }

    public TransactionIsolation getIsolationLevel() {
        return isolationLevel;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
