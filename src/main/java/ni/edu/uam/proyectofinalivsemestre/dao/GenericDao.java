package ni.edu.uam.proyectofinalivsemestre.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;

import java.util.List;

/**
 * DAO Genérico base para operaciones CRUD completas con Hibernate 6 y JPA.
 * Gestiona el ciclo de vida de transacciones (begin, commit, rollback)
 * y el cierre adecuado de cada EntityManager.
 *
 * @param <T>  Tipo de la entidad JPA
 * @param <ID> Tipo del identificador primario (Integer, Long, etc.)
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
public abstract class GenericDao<T, ID> {

    protected final Class<T> entityClass;

    protected GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Inserta una nueva entidad en la base de datos (Persist).
     */
    public T create(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ [GenericDao.create] Error al guardar " + entityClass.getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Busca una entidad por su clave primaria.
     */
    public T findById(ID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * Lista todas las entidades de la tabla.
     */
    public List<T> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza una entidad existente (Merge / Upsert).
     */
    public T update(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ [GenericDao.update] Error al actualizar " + entityClass.getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Elimina una entidad por su identificador primario.
     */
    public boolean deleteById(ID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("❌ [GenericDao.deleteById] Error al eliminar " + entityClass.getSimpleName() + " con id " + id + ": " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Guarda o actualiza (Upsert): si existe merge, sino persist.
     */
    public T saveOrUpdate(T entity) {
        return update(entity);
    }
}
