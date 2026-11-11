package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Match;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Match implementado con Hibernate 6 / JPA.
 * Contiene la CONSULTA HQL COMPLEJA 2 requerida para la Pantalla Principal / En Vivo.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class MatchDao extends GenericDao<Match, Integer> {

    public MatchDao() {
        super(Match.class);
    }

    // =========================================================================
    // CONSULTA HQL COMPLEJA OBLIGATORIA #2
    // =========================================================================
    /**
     * CONSULTA HQL 2:
     * Obtiene los partidos de una competición con sus equipos (local y visitante)
     * y la lista completa de eventos del partido (goles, tarjetas), cargados
     * en una sola consulta mediante JOIN FETCH para eliminar por completo
     * el problema de rendimiento N+1 de Hibernate.
     *
     * @param competitionId ID oficial de la liga (2014, 2021 o 2001)
     * @return Lista de partidos con todas sus relaciones hidratadas
     */
    public List<Match> findMatchesWithEventsAndTeams(int competitionId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT DISTINCT m FROM Match m " +
                         "JOIN FETCH m.competition c " +
                         "JOIN FETCH m.homeTeam ht " +
                         "JOIN FETCH m.awayTeam at " +
                         "LEFT JOIN FETCH m.events e " +
                         "WHERE c.id = :compId " +
                         "ORDER BY m.utcDate DESC";

            return em.createQuery(hql, Match.class)
                    .setParameter("compId", competitionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta HQL para partidos EN VIVO (status = 'IN_PLAY') con equipos y eventos,
     * vital para el refresco en tiempo real de la Pantalla 1.
     */
    public List<Match> findLiveMatchesWithEvents() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT DISTINCT m FROM Match m " +
                         "JOIN FETCH m.competition c " +
                         "JOIN FETCH m.homeTeam ht " +
                         "JOIN FETCH m.awayTeam at " +
                         "LEFT JOIN FETCH m.events e " +
                         "WHERE m.status = 'IN_PLAY' " +
                         "ORDER BY m.utcDate ASC";

            return em.createQuery(hql, Match.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene partidos por número de jornada (Matchday) con sus equipos.
     */
    public List<Match> findByMatchday(int competitionId, int matchday) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT DISTINCT m FROM Match m " +
                         "JOIN FETCH m.homeTeam ht " +
                         "JOIN FETCH m.awayTeam at " +
                         "WHERE m.competition.id = :compId AND m.matchday = :mday " +
                         "ORDER BY m.utcDate ASC";

            return em.createQuery(hql, Match.class)
                    .setParameter("compId", competitionId)
                    .setParameter("mday", matchday)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
