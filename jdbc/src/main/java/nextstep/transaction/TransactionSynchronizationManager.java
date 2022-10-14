package nextstep.transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.core.NamedThreadLocal;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal<>("Transactional resources");

    public static Object getResource(final Object key) {
        Map<Object, Object> primitiveResources = resources.get();
        if (primitiveResources == null) {
            return null;
        }
        return primitiveResources.getOrDefault(key, null);
    }

    public static void bindConnection(final DataSource dataSource, final Connection connection) {
        Map<Object, Object> primitiveResources = resources.get();
        if (primitiveResources == null) {
            primitiveResources = new HashMap<>();
            resources.set(primitiveResources);
        }
        primitiveResources.put(dataSource, connection);
    }

    public static Object release(final Object key) {
        Map<Object, Object> primitiveResources = resources.get();
        if (primitiveResources == null) {
            resources.remove();
            return null;
        }
        return primitiveResources.remove(key);
    }

    private TransactionSynchronizationManager() {
    }
}
