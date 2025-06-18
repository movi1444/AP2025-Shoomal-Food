package com.aut.shoomal.dao.impl;
import com.aut.shoomal.dao.GenericDao;
import com.aut.shoomal.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.lang.reflect.ParameterizedType;

public class GenericDaoImpl<T> implements GenericDao<T>
{
    private final Class<T> entityClass;
    @SuppressWarnings("unchecked")
    public GenericDaoImpl()
    {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public void create(T entity)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            create(entity, session);
            transaction.commit();
            System.out.println(entityClass.getSimpleName() + " added successfully!");
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error adding " + entity.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void create(T entity, Session session)
    {
        session.persist(entity);
    }

    @Override
    public void delete(Long id)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T entity = session.get(entityClass, id);
            if (entity != null)
            {
                session.remove(entity);
                transaction.commit();
                System.out.println(entityClass.getSimpleName() + " deleted successfully!");
            }
            else
                System.out.println(entityClass.getSimpleName() + "with ID " + id + " not found.");
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error deleting " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(T entity)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            update(entity, session);
            transaction.commit();
            System.out.println(entityClass.getSimpleName() + " updated successfully!");
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error updating " + entity.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(T entity, Session session)
    {
        session.merge(entity);
    }

    @Override
    public T findById(Long id)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
             return session.get(entityClass, id);
        } catch (Exception e) {
            System.err.println("Error finding " + entityClass.getSimpleName() + "by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<T> findAll()
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            CriteriaQuery<T> all = cq.select(root);
            return session.createQuery(all).list();
        } catch (Exception e) {
            System.err.println("Error finding all " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}