package nextstep.jdbc.fixture;

import java.util.Objects;

public class Tester {

    public Long id;
    public String name;

    public Tester(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Tester tester = (Tester) o;
        return Objects.equals(id, tester.id) && Objects.equals(name, tester.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
