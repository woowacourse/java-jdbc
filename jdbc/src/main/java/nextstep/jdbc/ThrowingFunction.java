package nextstep.jdbc;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

    R apply(final T t) throws E;
}
