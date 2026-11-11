package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Lineup;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Lineup con Hibernate 6 / JPA.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class LineupDao extends GenericDao<Lineup, Integer> {

    public LineupDao() {
        super(Lineup.class);
    }

    /**
     * Obtiene la alineación de un equipo para un partido específico.
     */
    public List<Lineup> findByMatchAndTeam(int matchId, int teamId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT l FROM Lineup l " +
                         "JOIN FETCH l.player p " +
                         "WHERE l.match.id = :matchId AND l.team.id = :teamId " +
                         "ORDER BY l.isStarter DESC, l.shirtNumber ASC";

            return em.createQuery(hql, Lineup.class)
                    .setParameter("matchId", matchId)
                    .setParameter("teamId", teamId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
