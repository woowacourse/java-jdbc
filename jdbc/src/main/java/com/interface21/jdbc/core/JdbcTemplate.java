package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.parameter.IntParameterSetter;
import com.interface21.jdbc.core.parameter.LongParameterSetter;
import com.interface21.jdbc.core.parameter.ObjectParameterSetter;
import com.interface21.jdbc.core.parameter.ParameterSetter;
import com.interface21.jdbc.core.parameter.StringParameterSetter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private static final List<ParameterSetter> PARAMETER_SETTERS = List.of(
            new IntParameterSetter(), new LongParameterSetter(), new StringParameterSetter());
    private static final ParameterSetter DEFAULT_PARAMETER_SETTER = new ObjectParameterSetter();

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(connection, sql, parameters);
             ResultSet resultSet = pstmt.executeQuery()) {
            log.debug("query = {}, {}", sql, Arrays.toString(parameters));

            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet, 1));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(connection, sql, parameters);
             ResultSet resultSet = pstmt.executeQuery()) {
            log.debug("query = {}, {}", sql, Arrays.toString(parameters));

            List<T> result = new ArrayList<>();
            if (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, 1));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(connection, sql, parameters)) {
            log.debug("query = {}, {}", sql, Arrays.toString(parameters));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Object[] parameters)
            throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        setPreparedStatement(pstmt, parameters);
        return pstmt;
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object[] parameters) throws SQLException {
        for (int arrayIndex = 0; arrayIndex < parameters.length; arrayIndex++) {
            Object parameter = parameters[arrayIndex];
            ParameterSetter parameterSetter = PARAMETER_SETTERS.stream()
                    .filter(setter -> setter.isAvailableParameter(parameter))
                    .findAny()
                    .orElse(DEFAULT_PARAMETER_SETTER);
            parameterSetter.setParameter(pstmt, toParameterIndex(arrayIndex), parameter);
        }
    }

    private int toParameterIndex(int arrayIndex) {
        return arrayIndex + 1;
    }
}
