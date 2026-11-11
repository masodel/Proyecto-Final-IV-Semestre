package ni.edu.uam.proyectofinalivsemestre.service.real;

import com.google.gson.*;
import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.model.dto.PartidoDetalle;
import ni.edu.uam.proyectofinalivsemestre.service.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Adaptador API -> transacción Hibernate -> modelos de lectura de la interfaz. */
public final class ServiciosReales implements PartidoService, PosicionService, FavoritoService {
    private final FootballApiService api;
    public ServiciosReales() { this(new FootballApiService(false)); }
    public ServiciosReales(FootballApiService api) { this.api = api; }
    public static String codigo(String liga) {
        return switch (liga) {
            case "LaLiga" -> "PD"; case "Premier League" -> "PL"; case "Champions League" -> "CL";
            default -> throw new IllegalArgumentException("Liga no reconocida.");
        };
    }
    public static String nombre(String codigo) {
        return switch (codigo) { case "PD" -> "LaLiga"; case "PL" -> "Premier League"; case "CL" -> "Champions League"; default -> codigo; };
    }
    public static int temporada(LocalDate fecha) { return fecha.getMonthValue() >= 7 ? fecha.getYear() : fecha.getYear() - 1; }
    private static <T> T leer(Function<EntityManager,T> trabajo) {
        try (EntityManager em = HibernateUtil.getEntityManager()) { return trabajo.apply(em); }
    }
    private static <T> T escribir(Function<EntityManager,T> trabajo) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            em.getTransaction().begin();
            try { T valor = trabajo.apply(em); em.getTransaction().commit(); return valor; }
            catch (RuntimeException e) { if (em.getTransaction().isActive()) em.getTransaction().rollback(); throw e; }
        }
    }
    private static JsonObject objeto(JsonObject json, String campo) {
        return json != null && json.has(campo) && json.get(campo).isJsonObject() ? json.getAsJsonObject(campo) : new JsonObject();
    }
    private static String texto(JsonObject json, String campo, String defecto) {
        return json.has(campo) && !json.get(campo).isJsonNull() ? json.get(campo).getAsString() : defecto;
    }
    private static Integer numero(JsonObject json, String campo) {
        return json.has(campo) && !json.get(campo).isJsonNull() ? json.get(campo).getAsInt() : null;
    }
    private static int requerido(JsonObject json, String campo) {
        Integer n = numero(json, campo);
        if (n == null) throw new IllegalArgumentException("La API no entregó el campo " + campo + ".");
        return n;
    }
    private static JsonArray lista(JsonObject json, String campo) {
        return json.has(campo) && json.get(campo).isJsonArray() ? json.getAsJsonArray(campo) : new JsonArray();
    }
    private static JsonObject respuesta(String json) {
        try { return JsonParser.parseString(json).getAsJsonObject(); }
        catch (RuntimeException e) { throw new IllegalArgumentException("La API devolvió datos no válidos."); }
    }
    private static void snapshot(EntityManager em, String id, JsonObject json) {
        em.merge(new ApiSnapshot(id, json.toString()));
    }
    private static Competition competicion(EntityManager em, JsonObject json) {
        int id = requerido(json, "id");
        Competition c = em.find(Competition.class, id);
        boolean nuevo = c == null;
        if (nuevo) { c = new Competition(); c.setId(id); }
        c.setCode(texto(json, "code", "")); c.setName(nombre(c.getCode()));
        c.setEmblemUrl(texto(json, "emblem", null));
        if (nuevo) em.persist(c);
        return c;
    }
    private static Team equipo(EntityManager em, JsonObject json, Competition c) {
        int id = requerido(json, "id");
        Team t = em.find(Team.class, id);
        boolean nuevo = t == null;
        if (nuevo) { t = new Team(); t.setId(id); t.setCompetition(c); }
        t.setName(texto(json, "name", "Equipo"));
        t.setShortName(texto(json, "shortName", t.getName())); t.setTla(texto(json, "tla", null));
        t.setCrestUrl(texto(json, "crest", null)); t.setUpdatedAt(LocalDateTime.now());
        // Un equipo participa en liga y Champions; no sobrescribir su liga doméstica.
        if (!"CL".equals(c.getCode())) t.setCompetition(c);
        if (nuevo) em.persist(t);
        return t;
    }
    private static void guardarPartido(EntityManager em, JsonObject json, JsonObject fallback) {
        JsonObject liga = objeto(json, "competition");
        if (!liga.has("id")) liga = fallback;
        Competition c = competicion(em, liga);
        Team local = equipo(em, objeto(json, "homeTeam"), c), visitante = equipo(em, objeto(json, "awayTeam"), c);
        int id = requerido(json, "id");
        Match m = em.find(Match.class, id);
        boolean nuevo = m == null;
        if (nuevo) { m = new Match(); m.setId(id); }
        m.setCompetition(c); m.setHomeTeam(local); m.setAwayTeam(visitante);
        m.setUtcDate(Instant.parse(texto(json, "utcDate", "")).atOffset(ZoneOffset.UTC).toLocalDateTime());
        m.setStatus(texto(json, "status", "SCHEDULED")); m.setMatchday(numero(json, "matchday"));
        JsonObject score = objeto(objeto(json, "score"), "fullTime");
        m.setHomeScore(numero(score, "home")); m.setAwayScore(numero(score, "away")); m.setUpdatedAt(LocalDateTime.now());
        if (nuevo) em.persist(m);
        snapshot(em, "match/" + id, json);
    }
    private static Equipo equipoUi(Team t) {
        return new Equipo((long)t.getId(), t.getShortName() == null ? t.getName() : t.getShortName());
    }
    public static String estadoUi(String status) {
        return switch (status) {
            case "IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTY_SHOOTOUT" -> "EN_VIVO";
            case "FINISHED", "AWARDED" -> "FINALIZADO";
            case "POSTPONED" -> "APLAZADO"; case "SUSPENDED" -> "SUSPENDIDO"; case "CANCELLED" -> "CANCELADO";
            default -> "PROGRAMADO";
        };
    }
    private static Partido partidoUi(Match m) {
        return new Partido((long)m.getId(), m.getUtcDate().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                new Liga(nombre(m.getCompetition().getCode())), equipoUi(m.getHomeTeam()), equipoUi(m.getAwayTeam()),
                m.getHomeScore(), m.getAwayScore(), estadoUi(m.getStatus()), null);
    }
    @Override public List<Partido> buscar(String liga, LocalDate fecha) {
        return leer(em -> {
            var q = em.createQuery("select m from Match m join fetch m.competition join fetch m.homeTeam join fetch m.awayTeam "
                    + "where m.utcDate >= :desde and m.utcDate < :hasta "
                    + (liga == null ? "" : "and m.competition.code = :liga ") + "order by m.utcDate", Match.class);
            ZoneId zona = ZoneId.systemDefault();
            q.setParameter("desde", fecha.atStartOfDay(zona).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            q.setParameter("hasta", fecha.plusDays(1).atStartOfDay(zona).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            if (liga != null) q.setParameter("liga", codigo(liga));
            // El esquema original incluía ejemplos: solo mostrar partidos confirmados por la API.
            return q.getResultList().stream().filter(m -> em.find(ApiSnapshot.class, "match/" + m.getId()) != null)
                    .map(ServiciosReales::partidoUi).toList();
        });
    }
    @Override public synchronized void sincronizar(String liga, LocalDate fecha) throws Exception {
        List<JsonObject> respuestas = new ArrayList<>();
        for (String code : liga == null ? List.of("PD","PL","CL") : List.of(codigo(liga))) {
            JsonObject json = respuesta(api.consultarRecurso("competitions/" + code + "/matches?season=" + temporada(fecha)));
            if (!json.has("matches")) throw new IllegalArgumentException("La respuesta no contiene partidos.");
            respuestas.add(json);
        }
        // Todas las respuestas llegan antes de abrir la transacción: un fallo no borra la caché.
        escribir(em -> {
            for (JsonObject json : respuestas) for (JsonElement match : lista(json,"matches"))
                guardarPartido(em, match.getAsJsonObject(), objeto(json,"competition"));
            return null;
        });
    }
    @Override public synchronized void actualizarDetalle(Long id) throws Exception {
        JsonObject json = respuesta(api.consultarRecurso("matches/" + id));
        escribir(em -> { guardarPartido(em, json, new JsonObject()); snapshot(em, "detail/" + id, json); return null; });
    }
    @Override public PartidoDetalle detalle(Long id) {
        return leer(em -> {
            Match m = em.find(Match.class, Math.toIntExact(id));
            if (m == null) throw new IllegalArgumentException("El partido no está guardado. Actualiza la lista.");
            ApiSnapshot s = em.find(ApiSnapshot.class, "detail/" + id);
            if (s == null) s = em.find(ApiSnapshot.class, "match/" + id);
            JsonObject json = s == null ? new JsonObject() : respuesta(s.getPayload());
            JsonObject homeStats = objeto(objeto(json,"homeTeam"),"statistics");
            JsonObject awayStats = objeto(objeto(json,"awayTeam"),"statistics");
            List<String> eventos = new ArrayList<>();
            for (JsonElement e : lista(json,"goals")) {
                JsonObject gol = e.getAsJsonObject();
                eventos.add(texto(gol,"minute","?") + "' · Gol · " + texto(objeto(gol,"team"),"name",""));
            }
            for (JsonElement e : lista(json,"bookings")) {
                JsonObject tarjeta = e.getAsJsonObject();
                eventos.add(texto(tarjeta,"minute","?") + "' · Tarjeta · " + texto(objeto(tarjeta,"team"),"name",""));
            }
            if (eventos.isEmpty()) eventos.add("La API no proporciona eventos para este partido.");
            Partido p = partidoUi(m);
            p = new Partido(p.getId(),p.getFecha(),p.getLiga(),p.getLocal(),p.getVisitante(),
                    p.getGolesLocal(),p.getGolesVisitante(),p.getEstado(),numero(json,"minute"));
            return new PartidoDetalle(p, texto(json,"venue","No disponible"), eventos,
                    numero(homeStats,"ball_possession"), numero(homeStats,"shots"), numero(awayStats,"shots"),
                    numero(homeStats,"shots_on_goal"), numero(awayStats,"shots_on_goal"));
        });
    }
    private static String claveTabla(String liga, String temporada) { return "standings/" + codigo(liga) + "/" + temporada; }
    private static List<Posicion> posiciones(JsonObject json) {
        List<Posicion> filas = new ArrayList<>();
        for (JsonElement e : lista(json,"standings")) {
            JsonObject tabla = e.getAsJsonObject();
            if (!"TOTAL".equals(texto(tabla,"type",""))) continue;
            for (JsonElement r : lista(tabla,"table")) {
                JsonObject row = r.getAsJsonObject(), t = objeto(row,"team");
                filas.add(new Posicion(requerido(row,"position"), new Equipo((long)requerido(t,"id"), texto(t,"shortName",texto(t,"name",""))),
                        requerido(row,"playedGames"), requerido(row,"won"), requerido(row,"draw"), requerido(row,"lost"),
                        requerido(row,"goalsFor"), requerido(row,"goalsAgainst"), requerido(row,"goalDifference"), requerido(row,"points")));
            }
        }
        return filas;
    }
    @Override public List<Posicion> tabla(String liga, String temporada) {
        return leer(em -> {
            ApiSnapshot s = em.find(ApiSnapshot.class, claveTabla(liga,temporada));
            return s == null ? List.of() : posiciones(respuesta(s.getPayload()));
        });
    }
    @Override public synchronized void sincronizar(String liga, String temporada) throws Exception {
        if (!temporada.matches("\\d{4}/\\d{2}")) throw new IllegalArgumentException("Temporada no válida.");
        JsonObject json = respuesta(api.consultarRecurso("competitions/" + codigo(liga) + "/standings?season=" + temporada.substring(0,4)));
        if (!json.has("standings")) throw new IllegalArgumentException("La API no devolvió una clasificación.");
        List<Posicion> filas = posiciones(json);
        escribir(em -> {
            Competition c = competicion(em, objeto(json,"competition"));
            for (JsonElement e : lista(json,"standings")) for (JsonElement r : lista(e.getAsJsonObject(),"table"))
                equipo(em, objeto(r.getAsJsonObject(),"team"), c);
            snapshot(em, claveTabla(liga,temporada),json);
            // Standing no tiene temporada: solo refleja la actual; el histórico queda en snapshots por temporada.
            if (Integer.parseInt(temporada.substring(0,4)) == temporada(LocalDate.now())) {
                em.createQuery("delete from Standing s where s.competition.id = :id").setParameter("id",c.getId()).executeUpdate();
                for (Posicion f : filas) em.persist(new Standing(c, em.getReference(Team.class,Math.toIntExact(f.getEquipo().getId())),
                        f.getPosicion(),f.getPartidosJugados(),f.getGanados(),f.getEmpatados(),f.getPerdidos(),f.getPuntos(),
                        f.getGolesFavor(),f.getGolesContra(),f.getDiferencia()));
            }
            return null;
        });
    }
    @Override public Set<Long> idsPartidosFavoritos() {
        return leer(em -> em.createQuery("select f.match.id from MatchFavorite f",Integer.class).getResultList()
                .stream().map(Integer::longValue).collect(Collectors.toSet()));
    }
    @Override public Set<Long> idsEquiposFavoritos() {
        return leer(em -> em.createQuery("select f.team.id from Favorite f",Integer.class).getResultList()
                .stream().map(Integer::longValue).collect(Collectors.toSet()));
    }
    @Override public synchronized boolean alternarPartido(Long id) {
        return escribir(em -> {
            MatchFavorite f = em.find(MatchFavorite.class,Math.toIntExact(id));
            if (f != null) { em.remove(f); return false; }
            Match m = em.find(Match.class,Math.toIntExact(id));
            if (m == null) throw new IllegalArgumentException("Partido no encontrado.");
            em.persist(new MatchFavorite(m)); return true;
        });
    }
    @Override public synchronized boolean alternarEquipo(Long id) {
        return escribir(em -> {
            Team t = em.find(Team.class,Math.toIntExact(id));
            if (t == null) throw new IllegalArgumentException("Equipo no encontrado.");
            List<Favorite> existentes = em.createQuery("select f from Favorite f where f.team.id = :id",Favorite.class)
                    .setParameter("id",Math.toIntExact(id)).getResultList();
            if (!existentes.isEmpty()) { existentes.forEach(em::remove); t.setFavorite(false); return false; }
            em.persist(new Favorite(t,null)); t.setFavorite(true); return true;
        });
    }
    @Override public List<Partido> partidosFavoritos() {
        return leer(em -> em.createQuery("select f from MatchFavorite f join fetch f.match m join fetch m.competition "
                + "join fetch m.homeTeam join fetch m.awayTeam order by m.utcDate",MatchFavorite.class).getResultList()
                .stream().map(f -> partidoUi(f.getMatch())).toList());
    }
    @Override public List<Equipo> equiposFavoritos() {
        return leer(em -> em.createQuery("select f from Favorite f join fetch f.team order by f.team.name",Favorite.class)
                .getResultList().stream().map(f -> equipoUi(f.getTeam())).toList());
    }
}
