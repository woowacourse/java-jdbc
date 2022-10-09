package nextstep.transaction.support;

public enum TransactionPropagation {

    PROPAGATION_REQUIRED(0);

    private final int index;

    TransactionPropagation(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
