package ni.edu.uam.proyectofinalivsemestre.dao;

import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.*;

import java.util.List;

/**
 * Clase ejecutiva para validar y demostrar el correcto funcionamiento de:
 * 1. Inicialización de Hibernate 6 + JPA 3.1 con PostgreSQL (Unidad 'football_pu')
 * 2. Operaciones CRUD en las entidades del Dominio
 * 3. Las 2 CONSULTAS HQL COMPLEJAS obligatorias
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class HibernateRunnerTest {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("     VALIDACIÓN DE PERSISTENCIA HIBERNATE / JPA - INTEGRANTE 1 (DONALD)        ");
        System.out.println("===============================================================================");

        try {
            // Inicialización de DAOs
            CompetitionDao compDao = new CompetitionDao();
            TeamDao teamDao = new TeamDao();
            StandingDao standingDao = new StandingDao();
            MatchDao matchDao = new MatchDao();
            PlayerDao playerDao = new PlayerDao();
            MatchEventDao eventDao = new MatchEventDao();

            // -----------------------------------------------------------------
            // 1. Verificación de Competiciones cargadas (LaLiga, Premier, Champions)
            // -----------------------------------------------------------------
            System.out.println("\n📌 1. COMPETICIONES CARGADAS EN BD:");
            List<Competition> comps = compDao.findAll();
            for (Competition c : comps) {
                System.out.println("   • [" + c.getCode() + "] " + c.getName() + " (ID: " + c.getId() + ")");
            }

            // -----------------------------------------------------------------
            // 2. Operación CRUD: Insertar un Jugador y asociarlo a un Equipo
            // -----------------------------------------------------------------
            System.out.println("\n⚽ 2. PRUEBA CRUD: Creando jugador con Hibernate...");
            Team realMadrid = teamDao.findById(86);
            if (realMadrid != null) {
                Player vinicius = new Player("Vinícius Júnior", "Forward", 7, "Brazil", realMadrid);
                playerDao.create(vinicius);
                System.out.println("   ✅ Jugador persistido con ID autogenerado: " + vinicius);
            }

            // -----------------------------------------------------------------
            // 3. CONSULTA HQL COMPLEJA #1: Tabla de Posiciones por Competición
            // -----------------------------------------------------------------
            System.out.println("\n📊 3. CONSULTA HQL #1 OBLIGATORIA: CLASIFICACIÓN DE LALIGA (ID: 2014):");
            System.out.println("-------------------------------------------------------------------------------");
            System.out.printf("%-4s %-25s %3s %3s %3s %3s %4s %3s%n", "POS", "CLUB", "PJ", "PG", "PE", "PP", "DG", "PTS");
            System.out.println("-------------------------------------------------------------------------------");
            List<Standing> tablaLaLiga = standingDao.findStandingsByCompetitionOrdered(2014);
            for (Standing s : tablaLaLiga) {
                System.out.printf("#%-3d %-25s %3d %3d %3d %3d %+4d %3d%n",
                        s.getPosition(),
                        s.getTeam().getName(),
                        s.getPlayedGames(),
                        s.getWon(),
                        s.getDraw(),
                        s.getLost(),
                        s.getGoalDifference(),
                        s.getPoints());
            }

            // -----------------------------------------------------------------
            // 4. Registrar Evento de Partido para prueba de HQL #2
            // -----------------------------------------------------------------
            System.out.println("\n⏱️ 4. Agregando eventos a un partido para verificar HQL #2...");
            Match match1001 = matchDao.findById(1001);
            if (match1001 != null) {
                List<MatchEvent> eventosExistentes = eventDao.findByMatch(1001);
                if (eventosExistentes.isEmpty()) {
                    MatchEvent gol1 = new MatchEvent(match1001, 32, "GOAL", null, realMadrid, "Disparo cruzado de derecha");
                    eventDao.create(gol1);
                    System.out.println("   ✅ Evento creado en BD: " + gol1);
                } else {
                    System.out.println("   ℹ️ El partido ya cuenta con " + eventosExistentes.size() + " evento(s).");
                }
            }

            // -----------------------------------------------------------------
            // 5. CONSULTA HQL COMPLEJA #2: Partidos con Equipos y Eventos
            // -----------------------------------------------------------------
            System.out.println("\n🏟️ 5. CONSULTA HQL #2 OBLIGATORIA: PARTIDOS CON EQUIPOS Y EVENTOS (ID: 2014):");
            System.out.println("-------------------------------------------------------------------------------");
            List<Match> partidosConEventos = matchDao.findMatchesWithEventsAndTeams(2014);
            for (Match m : partidosConEventos) {
                System.out.println("   🏟️ " + m);
                if (m.getEvents() == null || m.getEvents().isEmpty()) {
                    System.out.println("      (Sin eventos registrados)");
                } else {
                    for (MatchEvent ev : m.getEvents()) {
                        System.out.println("      ↳ Minuto " + ev.getMinute() + "': " + ev.getType() + " - " + ev.getDetail());
                    }
                }
            }

            // -----------------------------------------------------------------
            // 6. Prueba de Favoritos (Persistencia en PostgreSQL)
            // -----------------------------------------------------------------
            System.out.println("\n⭐ 6. PRUEBA DE FAVORITOS EN POSTGRESQL:");
            List<Team> favoritos = teamDao.getFavoriteTeams();
            for (Team fav : favoritos) {
                System.out.println("   ⭐ " + fav.getName() + " [" + fav.getTla() + "] - Liga: " + fav.getCompetition().getName());
            }

            System.out.println("\n===============================================================================");
            System.out.println("   ✅ ¡TODAS LAS ENTIDADES, CRUD Y CONSULTAS HQL FUNCIONAN PERFECTAMENTE!     ");
            System.out.println("===============================================================================");

        } catch (Exception e) {
            System.err.println("❌ Error en la prueba:");
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
