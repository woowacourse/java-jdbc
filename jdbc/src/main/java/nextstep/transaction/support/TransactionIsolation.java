package nextstep.transaction.support;

public enum TransactionIsolation {

    READ_UNCOMMITTED(4);

    private final int index;

    TransactionIsolation(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
