package ni.edu.uam.proyectofinalivsemestre.config;
import jakarta.persistence.*;
import java.util.*;

/** Factory perezosa: un fallo de conexión permite reintentar. */
public final class HibernateUtil {
    private static EntityManagerFactory factory;
    private HibernateUtil() {}
    public static synchronized EntityManager getEntityManager() {
        if (factory == null || !factory.isOpen()) {
            Map<String, Object> opciones = new HashMap<>();
            opciones.put("jakarta.persistence.jdbc.url", LocalConfig.valor("DB_URL", "jdbc:postgresql://127.0.0.1:5432/football_db"));
            opciones.put("jakarta.persistence.jdbc.user", LocalConfig.valor("DB_USER", "postgres"));
            opciones.put("jakarta.persistence.jdbc.password", LocalConfig.valor("DB_PASSWORD", ""));
            opciones.put("hibernate.show_sql", "false");
            opciones.put("hibernate.connection.connectTimeout", "10");
            opciones.put("hibernate.connection.socketTimeout", "30");
            try { factory = Persistence.createEntityManagerFactory("football_pu", opciones); }
            catch (RuntimeException e) {
                throw new IllegalStateException("No se pudo conectar con PostgreSQL. Revisa el servicio y la configuración local.");
            }
        }
        return factory.createEntityManager();
    }
    public static synchronized void shutdown() {
        if (factory != null && factory.isOpen()) factory.close();
        factory = null;
    }
}
