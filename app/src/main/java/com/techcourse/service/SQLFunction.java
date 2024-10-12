package com.techcourse.service;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T> {

    T apply() throws SQLException;
}
