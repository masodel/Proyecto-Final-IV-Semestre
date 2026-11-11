package ni.edu.uam.proyectofinalivsemestre.service.stub;

import ni.edu.uam.proyectofinalivsemestre.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class DatosDemo {
    public static final LocalDate FECHA = LocalDate.of(2026, 11, 10);
    public static volatile boolean FALLAR_SINCRONIZACION = Boolean.getBoolean("futbol.demo.fallar");
    static final Map<String, Equipo> EQUIPOS = new LinkedHashMap<>();
    static final List<Partido> PARTIDOS = new ArrayList<>();
    static final Set<Long> FAVORITOS_PARTIDOS = ConcurrentHashMap.newKeySet();
    static final Set<Long> FAVORITOS_EQUIPOS = ConcurrentHashMap.newKeySet();
    static {
        partido("14:00", "Champions League", "Inter", "Bayern", 1, 0, "EN_VIVO", 67);
        partido("16:30", "LaLiga", "Real Madrid", "Barcelona", 2, 1, "FINALIZADO", null);
        partido("18:00", "Premier League", "Arsenal", "Chelsea", null, null, "PROGRAMADO", null);
        partido("18:45", "LaLiga", "Sevilla", "Betis", null, null, "PROGRAMADO", null);
        partido("20:00", "Premier League", "Liverpool", "Man. City", null, null, "PROGRAMADO", null);
        partido("21:00", "Champions League", "PSG", "Atlético", null, null, "PROGRAMADO", null);
        for (String nombre : List.of("Athletic", "Villarreal", "Real Sociedad", "Girona")) equipo(nombre);
        FAVORITOS_PARTIDOS.addAll(List.of(1L, 2L));
        FAVORITOS_EQUIPOS.add(EQUIPOS.get("Real Madrid").getId());
    }
    private DatosDemo() {}
    static Equipo equipo(String nombre) {
        return EQUIPOS.computeIfAbsent(nombre, n -> new Equipo((long) EQUIPOS.size() + 1, n));
    }
    private static void partido(String hora, String liga, String local, String visitante,
                                Integer gl, Integer gv, String estado, Integer minuto) {
        PARTIDOS.add(new Partido((long) PARTIDOS.size() + 1, FECHA.atTime(java.time.LocalTime.parse(hora)),
                new Liga(liga), equipo(local), equipo(visitante), gl, gv, estado, minuto));
    }
    static void sincronizar() throws Exception {
        Thread.sleep(650);
        if (FALLAR_SINCRONIZACION) throw new java.io.IOException("Fallo de sincronización simulado. Se conservan los datos guardados.");
    }
}
