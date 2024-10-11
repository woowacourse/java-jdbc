package com.interface21.jdbc.support;

public class TestUser {

    private Long id;
    private String name;

    public TestUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TestUser(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
