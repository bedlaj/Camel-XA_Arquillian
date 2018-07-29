package eu.janbednar.module.domain.dao;

import eu.janbednar.module.domain.entity.TestEntity;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class TestDao extends AbstractDao {

    public List<TestEntity> getByValue(String valus){
        return getEntityManager().createQuery("select t from TestEntity t where t.value = :value", TestEntity.class).setParameter("value", valus).getResultList();
    }

    public <T> List<T> getByClass(Class<T> clazz){
        return getEntityManager().createQuery("select t from "+clazz.getSimpleName()+" t", clazz).getResultList();
    }

}
