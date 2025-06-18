package com.aut.shoomal.dao;
import com.aut.shoomal.Erfan.access.Role;

public interface RoleDao extends GenericDao<Role>
{
    Role findByName(String name);
    void addPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);
}