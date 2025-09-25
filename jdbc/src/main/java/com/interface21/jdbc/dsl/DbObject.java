package com.interface21.jdbc.dsl;

public class DbObject {

    private final Object object;

    public DbObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        if (object instanceof String) {
            return "'" + object + "'";
        } else {
            return object.toString();
        }
    }
}
