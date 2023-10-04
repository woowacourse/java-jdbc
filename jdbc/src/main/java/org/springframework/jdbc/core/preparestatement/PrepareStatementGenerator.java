package org.springframework.jdbc.core.preparestatement;

import java.sql.Connection;
import java.sql.PreparedStatement;

@FunctionalInterface
public interface PrepareStatementGenerator {

    PreparedStatement create(Connection connection, String sql);
}
