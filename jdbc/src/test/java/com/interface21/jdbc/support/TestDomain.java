package com.interface21.jdbc.support;

public class TestDomain {

    private final String name;
    private final long age;

    public TestDomain(String name, long age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public long getAge() {
        return age;
    }
}
