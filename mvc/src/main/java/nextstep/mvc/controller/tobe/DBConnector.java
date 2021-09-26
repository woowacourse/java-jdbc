package nextstep.mvc.controller.tobe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import javax.sql.DataSource;
import nextstep.web.annotation.ConnectingDB;
import nextstep.web.annotation.DBConnection;
import org.reflections.Reflections;

public class DBConnector {

    private final Reflections reflections;

    public DBConnector(Reflections reflections) {
        this.reflections = reflections;
    }

    public DataSource getDataSource()
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Set<Class<?>> DBConnectionInitiators = reflections.getTypesAnnotatedWith(
            ConnectingDB.class);
        Set<Class<?>> DBConnectionProvider = reflections.getTypesAnnotatedWith(DBConnection.class);

        DataSource dataSource = getRawDataSource(DBConnectionProvider);

        for (Class<?> clazz : DBConnectionInitiators) {
            initiateConnection(dataSource, clazz);
        }

        return dataSource;
    }

    private void initiateConnection(DataSource dataSource, Class<?> clazz)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method connectMethod = clazz.getMethod("execute", DataSource.class);
        connectMethod.invoke(clazz, dataSource);
    }

    private DataSource getRawDataSource(Set<Class<?>> DBConnectionProvider) {
        return DBConnectionProvider.stream()
            .findAny()
            .map(element -> {
                try {
                    Method method = element.getMethod("getInstance");
                    return (DataSource) method.invoke(element);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .orElseThrow(() -> new RuntimeException("DB 연결이 존재하지 않습니다."));
    }

}
