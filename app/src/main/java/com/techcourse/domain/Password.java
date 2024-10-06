package com.techcourse.domain;

import java.util.Objects;

public class Password {

    private final String value;

    public Password(final String value) {
        this.value = value;
    }

    public boolean matches(final String password) {
        return value.equals(password);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
