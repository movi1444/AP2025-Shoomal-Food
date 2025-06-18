package com.aut.shoomal.dao;
import org.hibernate.Session;

import java.util.List;

public interface GenericDao<T>
{
    void create(T entity);
    void create(T entity, Session session);
    void update(T entity);
    void update(T entity, Session session);
    void delete(Long id);
    T findById(Long id);
    List<T> findAll();
}