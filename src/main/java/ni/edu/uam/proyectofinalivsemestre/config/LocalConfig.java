package ni.edu.uam.proyectofinalivsemestre.config;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Secretos fuera del classpath: nunca se empaquetan en el JAR. */
public final class LocalConfig {
    private static final Properties LOCAL = cargar();
    private LocalConfig() {}
    private static Properties cargar() {
        Properties props = new Properties();
        Path archivo = Path.of(System.getProperty("futbol.config", "config.local.properties"));
        if (Files.exists(archivo)) try (Reader reader = Files.newBufferedReader(archivo)) {
            props.load(reader);
        } catch (IOException e) { throw new IllegalStateException("No se pudo leer la configuración local."); }
        return props;
    }
    public static String valor(String nombre, String defecto) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) valor = System.getProperty(nombre);
        if (valor == null || valor.isBlank()) valor = LOCAL.getProperty(nombre, defecto);
        return valor;
    }
}
