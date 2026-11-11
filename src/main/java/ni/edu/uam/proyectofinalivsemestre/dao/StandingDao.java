package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Standing;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Standing implementado con Hibernate 6 / JPA.
 * Contiene la CONSULTA HQL COMPLEJA 1 requerida para la Pantalla 2 (Tablas de Posiciones).
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class StandingDao extends GenericDao<Standing, Integer> {

    public StandingDao() {
        super(Standing.class);
    }

    // =========================================================================
    // CONSULTA HQL COMPLEJA OBLIGATORIA #1
    // =========================================================================
    /**
     * CONSULTA HQL 1:
     * Obtiene la clasificación oficial y ordenada de una de las 3 ligas
     * (LaLiga = 2014, Premier League = 2021, Champions League = 2001).
     *
     * Realiza JOIN FETCH con la entidad Team y Competition para precargar en memoria
     * los nombres de los equipos y escudos, aplicando un criterio estricto de desempate
     * futbolístico: Posición ASC, Puntos DESC, Diferencia de Gol DESC, Goles a Favor DESC.
     *
     * @param competitionId ID oficial de la competición
     * @return Lista ordenada de posiciones lista para inyectar en el TableView de JavaFX
     */
    public List<Standing> findStandingsByCompetitionOrdered(int competitionId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT s FROM Standing s " +
                         "JOIN FETCH s.team t " +
                         "JOIN FETCH s.competition c " +
                         "WHERE c.id = :compId " +
                         "ORDER BY s.position ASC, s.points DESC, s.goalDifference DESC, s.goalsFor DESC";

            return em.createQuery(hql, Standing.class)
                    .setParameter("compId", competitionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene los líderes/zona de Champions de una liga (top N equipos).
     */
    public List<Standing> findTopTeams(int competitionId, int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT s FROM Standing s " +
                         "JOIN FETCH s.team t " +
                         "WHERE s.competition.id = :compId " +
                         "ORDER BY s.position ASC";

            return em.createQuery(hql, Standing.class)
                    .setParameter("compId", competitionId)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
