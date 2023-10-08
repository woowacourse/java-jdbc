package org.springframework.transaction.support;

public class TransactionManager {

    private static final ThreadLocal<Boolean> transactionEnables = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> rollBackEnables = new ThreadLocal<>();

    private TransactionManager() {
    }

    public static void begin() {
        transactionEnables.set(Boolean.TRUE);
        rollBackEnables.set(Boolean.FALSE);
    }

    public static boolean isTransactionEnable() {
        final Boolean transactionEnable = transactionEnables.get();
        if (transactionEnable == null) {
            return false;
        }
        return transactionEnable;
    }

    public static boolean isRollbackEnable() {
        final Boolean rollbackEnable = rollBackEnables.get();
        if (rollbackEnable == null) {
            return false;
        }
        return rollbackEnable;
    }

    public static void setRollback() {
        validateTransactionEnable();
        rollBackEnables.set(Boolean.TRUE);
    }

    private static void validateTransactionEnable() {
        if (!isTransactionEnable()) {
            throw new IllegalStateException("트렌젝션이 활성화 되어있지 않습니다.");
        }
    }

    public static void clear() {
        transactionEnables.remove();
        rollBackEnables.remove();
    }
}
