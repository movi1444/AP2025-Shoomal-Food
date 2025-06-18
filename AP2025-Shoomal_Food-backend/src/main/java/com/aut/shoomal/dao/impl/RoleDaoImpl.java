package com.aut.shoomal.dao.impl;
import com.aut.shoomal.Erfan.access.Permission;
import com.aut.shoomal.Erfan.access.Role;
import com.aut.shoomal.dao.RoleDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class RoleDaoImpl extends GenericDaoImpl<Role> implements RoleDao
{
    @Override
    public Role findByName(String name)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Role> query = session.createQuery("from Role where name = :name", Role.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error finding rule by name: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addPermissionToRole(Long roleId, Long permissionId)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Role role = session.get(Role.class, roleId);
            Permission permission = session.get(Permission.class, permissionId);
            if (role != null && permission != null)
            {
                role.addPermission(permission);
                session.merge(role);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error adding permission to rule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Role role = session.get(Role.class, roleId);
            Permission permission = session.get(Permission.class, permissionId);
            if (role != null && permission != null)
            {
                role.removePermission(permission);
                session.merge(role);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error removing permission from rule: " + e.getMessage());
            e.printStackTrace();
        }
    }
}