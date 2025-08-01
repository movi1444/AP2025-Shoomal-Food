package com.aut.shoomal.entity.user;
import com.aut.shoomal.entity.user.access.Role;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue(value = "COURIER")
public class Courier extends User
{
    private Boolean approved = false;
    public Courier() {}
    public Courier(String name, String phoneNumber, String password, String email, Role role)
    {
        super(name, phoneNumber, password, email, role);
    }

    public boolean isApproved() {
        return approved;
    }
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
