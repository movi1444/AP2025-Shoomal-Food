package com.aut.shoomal.entity.user.access;
import com.aut.shoomal.dao.PermissionDao;

public class PermissionManager
{
    private final PermissionDao permissionDao;
    public PermissionManager(PermissionDao permissionDao)
    {
        this.permissionDao = permissionDao;
    }

    public void addPermission(Permission permission)
    {
        this.permissionDao.create(permission);
    }

    public void deletePermission(Permission permission)
    {
        this.permissionDao.create(permission);
    }

    public Permission findPermissionById(Long permissionId)
    {
        return this.permissionDao.findById(permissionId);
    }

    public Permission findPermissionByName(String permissionName)
    {
        return this.permissionDao.findByName(permissionName);
    }
}
