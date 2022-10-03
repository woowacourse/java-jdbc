package nextstep.jdbc;

@FunctionalInterface
public interface TriConsumer<T, U, R> {

    void apply(T t, U u, R r);
}
