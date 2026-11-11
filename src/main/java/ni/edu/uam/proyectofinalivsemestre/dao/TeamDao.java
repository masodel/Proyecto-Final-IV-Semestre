package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Team;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Team y gestión de favoritos
 * implementado con Hibernate 6 / JPA.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class TeamDao extends GenericDao<Team, Integer> {

    public TeamDao() {
        super(Team.class);
    }

    /**
     * Obtiene todos los equipos registrados en la base de datos ordenados alfabéticamente
     * con su competición precargada.
     */
    public List<Team> getAllTeams() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT t FROM Team t JOIN FETCH t.competition c ORDER BY t.name ASC";
            return em.createQuery(hql, Team.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene los equipos pertenecientes a una competición específica mediante HQL.
     * @param competitionId ID de la liga (2014=LaLiga, 2021=Premier, 2001=Champions)
     */
    public List<Team> getTeamsByCompetition(int competitionId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT t FROM Team t JOIN FETCH t.competition c WHERE c.id = :compId ORDER BY t.name ASC";
            return em.createQuery(hql, Team.class)
                    .setParameter("compId", competitionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene únicamente los equipos marcados como favoritos por el usuario mediante HQL
     * precargando su competición para evitar LazyInitializationException.
     */
    public List<Team> getFavoriteTeams() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String hql = "SELECT t FROM Team t JOIN FETCH t.competition c WHERE t.isFavorite = true ORDER BY t.name ASC";
            return em.createQuery(hql, Team.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca un equipo por su ID primario precargando su competición.
     */
    public Team getTeamById(int teamId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Team> list = em.createQuery(
                    "SELECT t FROM Team t JOIN FETCH t.competition c WHERE t.id = :id", Team.class)
                    .setParameter("id", teamId)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza el estado de favorito de un equipo (TRUE / FALSE) mediante transacción JPA.
     */
    public boolean updateFavoriteStatus(int teamId, boolean isFavorite) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Team team = em.find(Team.class, teamId);
            if (team != null) {
                team.setFavorite(isFavorite);
                team.setUpdatedAt(LocalDateTime.now());
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ Error en updateFavoriteStatus: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Guarda o actualiza un equipo (Upsert vía merge).
     */
    public boolean saveTeam(Team team) {
        try {
            saveOrUpdate(team);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
