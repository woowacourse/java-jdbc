package com.techcourse.service;

import java.sql.Connection;

@FunctionalInterface
public interface ServiceLogic {

    void execute(Connection connection);
}
