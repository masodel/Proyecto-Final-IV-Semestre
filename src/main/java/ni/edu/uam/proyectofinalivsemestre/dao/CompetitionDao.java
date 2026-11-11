package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.Competition;

import java.util.List;

/**
 * DAO JPA para la entidad Competition (LaLiga, Premier League, UEFA Champions League).
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public class CompetitionDao extends GenericDao<Competition, Integer> {

    public CompetitionDao() {
        super(Competition.class);
    }

    /**
     * Busca una competición por su código corto ('PD', 'PL', 'CL').
     */
    public Competition findByCode(String code) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Competition> list = em.createQuery(
                    "SELECT c FROM Competition c WHERE c.code = :code", Competition.class)
                    .setParameter("code", code)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }
}
