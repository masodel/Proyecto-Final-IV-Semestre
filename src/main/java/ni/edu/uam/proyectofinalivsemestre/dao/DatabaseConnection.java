package ni.edu.uam.proyectofinalivsemestre.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor centralizado de conexiones JDBC hacia PostgreSQL.
 * Diseñado y configurado para autenticación en modo 'trust' (local sin contraseña requerida).
 */
public class DatabaseConnection {

    // En Windows, 127.0.0.1 fuerza IPv4, garantizando que aplique la regla 'trust' de pg_hba.conf
    private static final String URL = "jdbc:postgresql://127.0.0.1:5432/football_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "2103";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: Driver de PostgreSQL no encontrado en el classpath.");
            e.printStackTrace();
        }
    }

    private DatabaseConnection() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Retorna una nueva conexión activa hacia la base de datos football_db.
     * Recuerda usar siempre try-with-resources al llamar este método.
     *
     * @return Connection activa
     * @throws SQLException si ocurre un error de red o autenticación
     */
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("ssl", "false");

        return DriverManager.getConnection(URL, props);
    }

    /**
     * Método main para validar la conexión de forma inmediata.
     */
    public static void main(String[] args) {
        System.out.println("🔄 Probando conexión a PostgreSQL (" + URL + ")...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ ¡CONEXIÓN EXITOSA! La base de datos 'football_db' está lista.");
                System.out.println("📦 Catálogo actual: " + conn.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar a PostgreSQL:");
            System.err.println("Código SQL: " + e.getSQLState());
            System.err.println("Detalle: " + e.getMessage());
        }
    }
}
