package com.aut.shoomal.entity.user.access;
import com.aut.shoomal.entity.user.User;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> users = new HashSet<>();

    public Role() {}
    public Role(String name)
    {
        this.name = name;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return this.id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public Set<Permission> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions)
    {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission)
    {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission)
    {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    public Set<User> getUsers()
    {
        return users;
    }

    public void setUsers(Set<User> users)
    {
        this.users = users;
    }

    public void addUser(User user)
    {
        this.users.add(user);
        user.setRole(this);
    }

    public void removeUser(User user)
    {
        this.users.remove(user);
        user.setRole(null);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}