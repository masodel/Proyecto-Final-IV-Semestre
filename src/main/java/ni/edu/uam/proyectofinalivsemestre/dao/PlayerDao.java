package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Player;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Player con Hibernate 6 / JPA.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class PlayerDao extends GenericDao<Player, Integer> {

    public PlayerDao() {
        super(Player.class);
    }

    /**
     * Obtiene la plantilla completa de jugadores de un equipo específico.
     */
    public List<Player> findByTeam(int teamId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT p FROM Player p WHERE p.team.id = :teamId ORDER BY p.shirtNumber ASC, p.name ASC";
            return em.createQuery(hql, Player.class)
                    .setParameter("teamId", teamId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
