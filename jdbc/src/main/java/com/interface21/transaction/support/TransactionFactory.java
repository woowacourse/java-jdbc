package com.interface21.transaction.support;

import com.interface21.jdbc.exception.ConnectionFailException;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionFactory {

    public static Transaction create(final DataSource dataSource) {
        try {
            return new Transaction(dataSource.getConnection());
        } catch (SQLException e) {
            throw new ConnectionFailException("연결을 실패했습니다", e);
        }
    }

    private TransactionFactory() {}
}
