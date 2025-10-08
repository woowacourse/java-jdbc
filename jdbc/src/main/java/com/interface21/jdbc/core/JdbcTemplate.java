package com.interface21.jdbc.core;

import com.interface21.jdbc.core.preparedstatement.PreparedStatementInitializer;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record JdbcTemplate(DataSource dataSource) {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void execute(final PreparedStatementSpecification specification) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = PreparedStatementInitializer.initialize(
                        connection,
                        specification
                )
        ) {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> findOne(
            final PreparedStatementSpecification specification,
            final RowMapper<T> rowMapper
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = PreparedStatementInitializer.initialize(
                        connection,
                        specification
                )
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                T result = rowMapper.map(resultSet);
                if (resultSet.next()) {
                    throw new RuntimeException("쿼리 결과가 2개 이상입니다.");
                }
                return Optional.of(result);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> findAll(
            final PreparedStatementSpecification specification,
            final RowMapper<T> rowMapper
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = PreparedStatementInitializer.initialize(
                        connection,
                        specification
                )
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
