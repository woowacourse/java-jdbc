package org.springframework.jdbc.support;

import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallBack {

    void action() throws SQLException;
}
