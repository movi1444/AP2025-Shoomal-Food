package com.aut.shoomal.dao.impl;
import com.aut.shoomal.entity.user.access.Permission;
import com.aut.shoomal.dao.PermissionDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class PermissionDaoImpl extends GenericDaoImpl<Permission> implements PermissionDao
{
    @Override
    public Permission findByName(String name)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Permission> query = session.createQuery("from Permission where name = :name", Permission.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error finding permission: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
