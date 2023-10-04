package org.springframework.jdbc.core;

public class Member {

    private final Long id;
    private final String name;

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
