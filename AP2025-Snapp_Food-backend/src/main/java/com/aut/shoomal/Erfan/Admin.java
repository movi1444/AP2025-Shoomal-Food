package com.aut.shoomal.Erfan;
import com.aut.shoomal.Erfan.access.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "ADMIN")
public class Admin extends User
{
    public Admin() {}
    public Admin(String name, String phoneNumber, String password, String email, Role role)
    {
        super(name, phoneNumber, password, email, role);
    }
}
