package com.interface21.jdbc.core;

public class TestUser {

    private final Long id;
    private final String name;

    TestUser(Long id, String name) {
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
    public String toString() {
        return "TestUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
