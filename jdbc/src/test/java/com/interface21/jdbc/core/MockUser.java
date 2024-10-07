package com.interface21.jdbc.core;

import java.util.Objects;

public class MockUser {

    private final Long id;
    private final String name;

    public MockUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockUser mockUser = (MockUser) o;
        return Objects.equals(id, mockUser.id) && Objects.equals(name, mockUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
