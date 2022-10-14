package nextstep.jdbc.support;

@FunctionalInterface
public interface TransactionInvoker<T> {

    T invoke();
}
