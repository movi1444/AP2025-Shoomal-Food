package com.aut.shoomal.entity.user;
import com.aut.shoomal.entity.user.access.Role;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue(value = "BUYER")
public class Buyer extends User
{
    public Buyer() {}
    public Buyer(String name, String phoneNumber, String password, String email, Role role)
    {
        super(name, phoneNumber, password, email, role);
    }
}
