package ni.edu.uam.proyectofinalivsemestre.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ni.edu.uam.proyectofinalivsemestre.model.PartidoTemp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Servicio encargado del consumo y mapeo de datos desde football-data.org (v4)
 * o desde archivo local de prueba (Mock) para no agotar la cuota de la capa gratuita.
 *
 * Rol: Integrante 2 (Conector API)
 */
public class FootballApiService {

    // -------------------------------------------------------------------------
    // Constantes de Configuración de la API (Protegidas de Git)
    // -------------------------------------------------------------------------
    // Carga segura: busca en Variable de Entorno -> System Property -> config.properties (ignorado por Git)
    private static final String API_KEY = cargarApiKey();
    private static final String BASE_URL = "https://api.football-data.org/v4/";

    // Códigos oficiales de competiciones solicitadas
    public static final String COMPETICION_LALIGA = "PD";      // Primera División / LaLiga
    public static final String COMPETICION_PREMIER = "PL";     // Premier League
    public static final String COMPETICION_CHAMPIONS = "CL";   // UEFA Champions League

    private static final String ARCHIVO_MOCK_DEFAULT = "partidos_prueba.json";

    // -------------------------------------------------------------------------
    // Atributos de Instancia
    // -------------------------------------------------------------------------
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Flag de control: por defecto en TRUE para modo seguro (offline/mock)
     * para no agotar las 10 peticiones/minuto de la cuenta gratuita.
     */
    private boolean usarMockLocal = true;

    public FootballApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder().create();
    }

    public FootballApiService(boolean usarMockLocal) {
        this();
        this.usarMockLocal = usarMockLocal;
    }

    // -------------------------------------------------------------------------
    // Getters y Setters de Configuración
    // -------------------------------------------------------------------------
    public boolean isUsarMockLocal() {
        return usarMockLocal;
    }

    public void setUsarMockLocal(boolean usarMockLocal) {
        this.usarMockLocal = usarMockLocal;
    }

    // -------------------------------------------------------------------------
    // Método Principal de Orquestación
    // -------------------------------------------------------------------------
    /**
     * Obtiene los partidos de una competición. Prioriza el mock local si usarMockLocal está activo.
     *
     * @param codigoCompeticion Código de competición (ej. "PD", "PL", "CL")
     * @return Lista de PartidoTemp procesada y limpia
     */
    public List<PartidoTemp> obtenerPartidos(String codigoCompeticion) {
        if (usarMockLocal) {
            System.out.println("[FootballApiService] >> MODO PRUEBA ACTIVO: Leyendo archivo local '" + ARCHIVO_MOCK_DEFAULT + "'...");
            return obtenerPartidosDesdeArchivoPrueba(ARCHIVO_MOCK_DEFAULT);
        } else {
            System.out.println("[FootballApiService] >> MODO API REAL ACTIVO: Consultando " + BASE_URL + " para " + codigoCompeticion + "...");
            return obtenerPartidosDesdeApi(codigoCompeticion);
        }
    }

    // -------------------------------------------------------------------------
    // Obtención vía HTTP Client (football-data.org)
    // -------------------------------------------------------------------------
    /**
     * Realiza la llamada HTTP real a football-data.org utilizando HttpClient nativo de Java 17.
     *
     * @param codigoCompeticion Código de competición ("PD", "PL", "CL")
     * @return Lista de PartidoTemp
     */
    public List<PartidoTemp> obtenerPartidosDesdeApi(String codigoCompeticion) {
        if (API_KEY == null || API_KEY.isBlank()) {
            System.err.println("[ADVERTENCIA] No se detectó ninguna API Key válida.");
            System.err.println(">> Configúrala en 'src/main/resources/config.properties' o como variable de entorno 'FOOTBALL_API_KEY'.");
            System.err.println(">> Redirigiendo a datos de prueba locales...");
            return obtenerPartidosDesdeArchivoPrueba();
        }

        String endpoint = BASE_URL + "competitions/" + codigoCompeticion + "/matches";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("X-Auth-Token", API_KEY)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                System.err.println("[ERROR 429] Límite de peticiones alcanzado (10 req/min). Cambia a usarMockLocal = true.");
                return Collections.emptyList();
            }

            if (response.statusCode() != 200) {
                System.err.println("[ERROR " + response.statusCode() + "] Respuesta de API: " + response.body());
                return Collections.emptyList();
            }

            return parsearPartidos(response.body());

        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Excepción de conexión con la API: " + e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    // -------------------------------------------------------------------------
    // Obtención vía Archivo Local JSON (src/main/resources/)
    // -------------------------------------------------------------------------
    /**
     * Lee un archivo JSON local en src/main/resources/ y parsea sus partidos.
     *
     * @param nombreArchivo Nombre del archivo en resources (ej. "partidos_prueba.json")
     * @return Lista de PartidoTemp
     */
    public List<PartidoTemp> obtenerPartidosDesdeArchivoPrueba(String nombreArchivo) {
        try {
            String jsonContenido = leerArchivoRecurso(nombreArchivo);
            return parsearPartidos(jsonContenido);
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo leer el archivo de prueba: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Sobrecarga sin parámetros que usa el archivo de prueba por defecto.
     */
    public List<PartidoTemp> obtenerPartidosDesdeArchivoPrueba() {
        return obtenerPartidosDesdeArchivoPrueba(ARCHIVO_MOCK_DEFAULT);
    }

    /**
     * Lee un archivo de recursos con compatibilidad dual:
     * 1. ClassLoader (para empaquetado JAR o ejecución estándar de Maven/IntelliJ).
     * 2. Filesystem directo en src/main/resources/ como respaldo seguro.
     */
    private String leerArchivoRecurso(String nombreArchivo) throws IOException {
        String rutaNormalizada = nombreArchivo.startsWith("/") ? nombreArchivo : "/" + nombreArchivo;

        // Estrategia 1: Classpath
        try (InputStream is = FootballApiService.class.getResourceAsStream(rutaNormalizada)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // Estrategia 2: Filesystem fallback
        Path pathDirecto = Path.of("src", "main", "resources", nombreArchivo.replaceFirst("^/", ""));
        if (Files.exists(pathDirecto)) {
            return Files.readString(pathDirecto, StandardCharsets.UTF_8);
        }

        throw new FileNotFoundException("No se encontró el recurso '" + nombreArchivo + 
                "' ni en el Classpath ni en src/main/resources/");
    }

    // -------------------------------------------------------------------------
    // Mapeo JSON con Gson a List<PartidoTemp>
    // -------------------------------------------------------------------------
    /**
     * Parsea la respuesta JSON de football-data.org a una lista de PartidoTemp.
     * Extrae: Nombre de competición, equipo local, equipo visitante, estado y marcador final.
     *
     * @param json Cadena JSON cruda recibida de la API o del archivo
     * @return Lista de PartidoTemp
     */
    public List<PartidoTemp> parsearPartidos(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        ApiResponseDto responseDto = gson.fromJson(json, ApiResponseDto.class);
        if (responseDto == null || responseDto.matches() == null) {
            return Collections.emptyList();
        }

        String fallbackCompeticion = (responseDto.competition() != null && responseDto.competition().name() != null)
                ? responseDto.competition().name()
                : "Competición General";

        List<PartidoTemp> resultado = new ArrayList<>();
        for (MatchDto m : responseDto.matches()) {
            if (m == null) continue;

            // 1. Nombre de la competición
            String nombreComp = (m.competition() != null && m.competition().name() != null)
                    ? m.competition().name()
                    : fallbackCompeticion;

            // 2. Equipo local
            String local = (m.homeTeam() != null && m.homeTeam().name() != null)
                    ? m.homeTeam().name()
                    : "Local N/D";

            // 3. Equipo visitante
            String visitante = (m.awayTeam() != null && m.awayTeam().name() != null)
                    ? m.awayTeam().name()
                    : "Visitante N/D";

            // 4. Estado del partido
            String estado = (m.status() != null) ? m.status() : "UNKNOWN";

            // 5. Marcador final
            String marcadorFinal = "Pendiente";
            if (m.score() != null && m.score().fullTime() != null) {
                Integer golesLocal = m.score().fullTime().home();
                Integer golesVisitante = m.score().fullTime().away();
                if (golesLocal != null && golesVisitante != null) {
                    marcadorFinal = golesLocal + " - " + golesVisitante;
                }
            }

            resultado.add(new PartidoTemp(nombreComp, local, visitante, estado, marcadorFinal));
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // DTOs Auxiliares para Gson (Estructura football-data.org v4)
    // -------------------------------------------------------------------------
    public record ApiResponseDto(CompetitionDto competition, List<MatchDto> matches) {}
    public record CompetitionDto(Integer id, String name, String code) {}
    public record MatchDto(
            Integer id,
            String status,
            CompetitionDto competition,
            TeamDto homeTeam,
            TeamDto awayTeam,
            ScoreDto score
    ) {}
    public record TeamDto(Integer id, String name, String shortName, String tla) {}
    public record ScoreDto(ScoreTimeDto fullTime, String winner, String duration) {}
    public record ScoreTimeDto(Integer home, Integer away) {}

    // -------------------------------------------------------------------------
    // Carga Segura de API Key (Protección Git)
    // -------------------------------------------------------------------------
    /**
     * Carga de forma segura la API Key sin exponerla en el código ni en el repositorio.
     * Orden de búsqueda:
     * 1. Variable de Entorno: FOOTBALL_API_KEY
     * 2. Propiedad de JVM: -DFOOTBALL_API_KEY=...
     * 3. Archivo ignorado por Git: src/main/resources/config.properties
     */
    private static String cargarApiKey() {
        // 1. Variable de entorno (útil para IntelliJ Run Configurations o CI/CD)
        String envKey = System.getenv("FOOTBALL_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }

        // 2. Propiedad del sistema JVM
        String propKey = System.getProperty("FOOTBALL_API_KEY");
        if (propKey != null && !propKey.isBlank()) {
            return propKey.trim();
        }

        // 3. Archivo config.properties (en Classpath / resources)
        try (InputStream is = FootballApiService.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String fileKey = props.getProperty("FOOTBALL_API_KEY");
                if (fileKey != null && !fileKey.isBlank()) {
                    return fileKey.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // 4. Archivo config.properties (Filesystem fallback directo)
        Path pathDirecto = Path.of("src", "main", "resources", "config.properties");
        if (Files.exists(pathDirecto)) {
            try (InputStream is = Files.newInputStream(pathDirecto)) {
                Properties props = new Properties();
                props.load(is);
                String fileKey = props.getProperty("FOOTBALL_API_KEY");
                if (fileKey != null && !fileKey.isBlank()) {
                    return fileKey.trim();
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    // -------------------------------------------------------------------------
    // Método Main de Prueba Rápida (Listo para Run en IntelliJ)
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("       PRUEBA RÁPIDA - CONNECTOR FOOTBALL-DATA.ORG (INTEGRANTE 2)              ");
        System.out.println("===============================================================================");

        // Instancia del servicio con mock activo por defecto (seguro contra rate limit)
        FootballApiService service = new FootballApiService(true);

        System.out.println("1. Ejecutando lectura de partidos_prueba.json (Modo Seguro Offline)...");
        List<PartidoTemp> partidosPrueba = service.obtenerPartidos(COMPETICION_LALIGA);

        System.out.println("\n--- RESULTADOS OBTENIDOS (" + partidosPrueba.size() + " partidos parseados) ---");
        for (PartidoTemp p : partidosPrueba) {
            System.out.println(p);
        }

        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.println("Para probar la API real con tu API Key:");
        System.out.println("   service.setUsarMockLocal(false);");
        System.out.println("   List<PartidoTemp> reales = service.obtenerPartidos(FootballApiService.COMPETICION_LALIGA);");
        System.out.println("===============================================================================");
    }
}
