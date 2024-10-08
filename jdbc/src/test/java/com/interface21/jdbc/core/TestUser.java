package com.interface21.jdbc.core;

public class TestUser {
    private int id;
    private String name;

    public TestUser(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
