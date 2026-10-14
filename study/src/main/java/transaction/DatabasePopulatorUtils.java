package transaction;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class DatabasePopulatorUtils {

    public static void execute(final DataSource dataSource) {
        final var populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);
    }

    private DatabasePopulatorUtils() {}
}
