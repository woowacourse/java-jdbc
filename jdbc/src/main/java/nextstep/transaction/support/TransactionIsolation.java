package nextstep.transaction.support;

public enum TransactionIsolation {

    READ_UNCOMMITTED(4);

    private final int level;

    TransactionIsolation(final int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
