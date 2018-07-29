package eu.janbednar.module.domain.dao;

import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.persistence.*;

/**
 * Abstract class injecting EntityManager
 *
 * @author skultetye
 */

public abstract class AbstractDao {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION,
                        unitName = "ExampleDS-unit")
    protected EntityManager entityManager;

    /**
     * Return injected entity manager.
     *
     * @return Injected EntityManager
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * entitymanager.persist() wrapper
     *
     * @param entity entity to save
     */
    public <T> void save(T entity) {
        getEntityManager().persist(entity);
        LoggerFactory.getLogger(getClass()).info("save: "+entity);
    }
    
    /**
     * entitymanager.merge() wrapper
     *
     * @param entity entity to merge
     */
    public <T> T merge(T entity) {
        LoggerFactory.getLogger(getClass()).info("merge: "+entity);
        return getEntityManager().merge(entity);
    }

    public <T> T find(Class<T> clazz, Object id) {
        return getEntityManager().find(clazz,id);
    }

    /**
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> TypedQuery<T> createCatchableQuery(String query,Class<T> clazz){
        TypedQuery<T> q = getEntityManager().createQuery(query, clazz);
        q.setHint("org.hibernate.cacheable", true);
        return q;
    }

    /**
     *
     * @param query
     * @return
     */
    public Query createCatchableQuery(String query){
        Query q = getEntityManager().createQuery(query);
        q.setHint("org.hibernate.cacheable", true);
        return q;
    }
}
