package com.techcourse.support;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;
import java.util.UUID;

public final class TestDataSource {
    public static DataSource create() {
        final var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        source.setUser("");
        source.setPassword("");
        return source;
    }

    private TestDataSource() {}
}
