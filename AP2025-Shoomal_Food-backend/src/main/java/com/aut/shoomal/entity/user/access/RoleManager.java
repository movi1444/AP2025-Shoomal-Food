package com.aut.shoomal.entity.user.access;
import com.aut.shoomal.dao.RoleDao;

public class RoleManager
{
    private final RoleDao roleDao;
    public RoleManager(RoleDao roleDao)
    {
        this.roleDao = roleDao;
        //this.createDefaultRules();
    }

    private void createDefaultRules()
    {
        this.addRole(new Role("Admin"));
        this.addRole(new Role("Buyer"));
        this.addRole(new Role("Seller"));
        this.addRole(new Role("Courier"));
    }

    public void addRole(Role role)
    {
        roleDao.create(role);
    }

    public void updateRole(Role role)
    {
        roleDao.update(role);
    }

    public Role getRoleById(Long id)
    {
        return roleDao.findById(id);
    }

    public Role findRoleByName(String roleName)
    {
        return roleDao.findByName(roleName);
    }

    public void addPermissionToRole(Long roleId, Long permissionId)
    {
        roleDao.addPermissionToRole(roleId, permissionId);
    }

    public void removePermissionFromRole(Long roleId, Long permissionId)
    {
        roleDao.removePermissionFromRole(roleId, permissionId);
    }
}