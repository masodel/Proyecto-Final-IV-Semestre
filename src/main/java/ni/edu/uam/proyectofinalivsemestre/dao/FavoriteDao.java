package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Favorite;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Favorite con Hibernate 6 / JPA.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class FavoriteDao extends GenericDao<Favorite, Integer> {

    public FavoriteDao() {
        super(Favorite.class);
    }

    /**
     * Obtiene todos los favoritos del usuario cargando sus equipos correspondientes.
     */
    public List<Favorite> findAllWithTeams() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT f FROM Favorite f " +
                         "JOIN FETCH f.team t " +
                         "JOIN FETCH t.competition c " +
                         "ORDER BY f.createdAt DESC";

            return em.createQuery(hql, Favorite.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca si un equipo específico ya está en favoritos.
     */
    public Favorite findByTeamId(int teamId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Favorite> list = em.createQuery(
                    "SELECT f FROM Favorite f WHERE f.team.id = :teamId", Favorite.class)
                    .setParameter("teamId", teamId)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }
}
