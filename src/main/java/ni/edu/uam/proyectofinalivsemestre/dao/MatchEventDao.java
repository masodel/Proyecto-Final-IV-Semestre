package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.MatchEvent;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad MatchEvent con Hibernate 6 / JPA.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class MatchEventDao extends GenericDao<MatchEvent, Integer> {

    public MatchEventDao() {
        super(MatchEvent.class);
    }

    /**
     * Obtiene el listado cronológico de eventos (goles, tarjetas) de un partido.
     */
    public List<MatchEvent> findByMatch(int matchId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT e FROM MatchEvent e " +
                         "LEFT JOIN FETCH e.player " +
                         "JOIN FETCH e.team " +
                         "WHERE e.match.id = :matchId " +
                         "ORDER BY e.minute ASC";

            return em.createQuery(hql, MatchEvent.class)
                    .setParameter("matchId", matchId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
