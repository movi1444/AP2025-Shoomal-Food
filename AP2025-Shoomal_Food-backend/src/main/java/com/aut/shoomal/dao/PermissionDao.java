package com.aut.shoomal.dao;
import com.aut.shoomal.entity.user.access.Permission;

public interface PermissionDao extends GenericDao<Permission>
{
    Permission findByName(String name);
}
