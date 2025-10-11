package com.interface21.jdbc.core;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDataSourceConfig {
    
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private static DataSource dataSource;
    
    public static DataSource getInstance() {
        if (dataSource == null) {
            JdbcDataSource h2DataSource = new JdbcDataSource();
            h2DataSource.setURL(DB_URL);
            h2DataSource.setUser(DB_USER);
            h2DataSource.setPassword(DB_PASSWORD);
            dataSource = h2DataSource;
            
            // 테스트용 테이블 생성
            createTestTable();
        }
        return dataSource;
    }
    
    private static void createTestTable() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 기존 테이블 삭제
            stmt.execute("DROP TABLE IF EXISTS users");
            
            // 실제 users 테이블 구조에 맞춰 테이블 생성
            stmt.execute("""
                CREATE TABLE users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    account VARCHAR(50) NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL
                )
                """);
                
        } catch (SQLException e) {
            throw new RuntimeException("테스트 테이블 생성 실패", e);
        }
    }
    
    public static void clearTestData() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException("테스트 데이터 삭제 실패", e);
        }
    }
}
