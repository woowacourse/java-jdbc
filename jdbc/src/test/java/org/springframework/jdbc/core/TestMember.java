package org.springframework.jdbc.core;

public class TestMember {

    private final Long id;
    private final String name;

    public TestMember(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
