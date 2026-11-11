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
    // Carga segura: busca en Variable de Entorno -> System Property -> config.local.properties (fuera del classpath)

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
        try { return parsearPartidos(consultarRecurso("competitions/" + codigoCompeticion + "/matches")); }
        catch (IOException e) { throw new IllegalStateException(e.getMessage(), e); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La consulta a la API fue interrumpida.", e);
        }
    }

    /** No sustituye errores por listas vacías ni datos ficticios. */
    public String consultarRecurso(String recurso) throws IOException, InterruptedException {
        String token = ni.edu.uam.proyectofinalivsemestre.config.LocalConfig.valor("FOOTBALL_API_KEY", "");
        if (token.isBlank()) throw new IOException("Falta FOOTBALL_API_KEY en la configuración local.");
        if (!recurso.matches("(?:competitions/(?:PD|PL|CL)/(?:matches|standings)(?:\\?season=\\d{4})?|matches/\\d+)"))
            throw new IllegalArgumentException("Recurso de fútbol no permitido.");
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + recurso))
                .header("X-Auth-Token", token).header("Accept", "application/json")
                .timeout(Duration.ofSeconds(25)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String motivo = switch (response.statusCode()) {
                case 401 -> "La clave de la API no fue aceptada.";
                case 403 -> "Tu plan de API no permite consultar este recurso o temporada.";
                case 429 -> "Se alcanzó el límite de consultas. Espera un minuto antes de actualizar.";
                default -> "La API no está disponible (HTTP " + response.statusCode() + ").";
            };
            throw new IOException(motivo);
        }
        return response.body();
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
     * 3. Archivo ignorado por Git: config.local.properties
     */
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
