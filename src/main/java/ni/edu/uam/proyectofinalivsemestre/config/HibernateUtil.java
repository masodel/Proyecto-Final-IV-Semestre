package ni.edu.uam.proyectofinalivsemestre.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Fábrica centralizada y segura del EntityManager de Hibernate.
 * Implementa el patrón Singleton para la EntityManagerFactory (costosa de crear).
 *
 * Uso recomendado en los DAOs:
 * <pre>
 *   try (EntityManager em = HibernateUtil.getEntityManager()) {
 *       // ... operaciones JPA
 *   }
 * </pre>
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public final class HibernateUtil {

    /** Nombre de la unidad de persistencia definida en persistence.xml */
    private static final String PERSISTENCE_UNIT = "football_pu";

    /** Singleton de la factory — se crea una sola vez al cargar la clase */
    private static final EntityManagerFactory EMF;

    static {
        try {
            EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            System.out.println("[HibernateUtil] ✅ EntityManagerFactory inicializada correctamente.");
        } catch (Exception e) {
            System.err.println("[HibernateUtil] ❌ Error al inicializar Hibernate: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    // Constructor privado: clase utilitaria, no instanciable
    private HibernateUtil() {}

    /**
     * Retorna un nuevo EntityManager. Debe cerrarse con try-with-resources.
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /**
     * Libera todos los recursos de Hibernate al cerrar la aplicación.
     * Llama este método en el shutdown hook o en Application.stop() de JavaFX.
     */
    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
            System.out.println("[HibernateUtil] 🔒 EntityManagerFactory cerrada.");
        }
    }
}
