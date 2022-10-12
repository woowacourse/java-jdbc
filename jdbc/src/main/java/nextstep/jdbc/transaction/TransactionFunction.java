package nextstep.jdbc.transaction;

@FunctionalInterface
public interface TransactionFunction<T> {
    T execute() throws Exception;
}
