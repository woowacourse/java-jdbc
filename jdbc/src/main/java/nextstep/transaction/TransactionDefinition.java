package nextstep.transaction;

import nextstep.transaction.support.TransactionIsolation;
import nextstep.transaction.support.TransactionPropagation;

public interface TransactionDefinition {

    TransactionPropagation getPropagationBehavior();

    TransactionIsolation getIsolationLevel();

    boolean isReadOnly();
}
