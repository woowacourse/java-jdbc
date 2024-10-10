package com.interface21.jdbc.core.sample;

import java.util.Objects;

public class Snake  {

    private Long id;
    private String ownerName;

    public Snake(Long id, String ownerName) {
        this.id = id;
        this.ownerName = ownerName;
    }

    public Snake() {
        this(null, null);
    }

    public Long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Snake snake = (Snake) object;
        return Objects.equals(id, snake.id) && Objects.equals(ownerName, snake.ownerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerName);
    }
}
