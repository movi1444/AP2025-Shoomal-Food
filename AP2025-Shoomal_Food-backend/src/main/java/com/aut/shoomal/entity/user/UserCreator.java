package com.aut.shoomal.entity.user;
import com.aut.shoomal.entity.user.access.Role;

public class UserCreator
{
    public static User createUser(UserTypes type, String name, String phoneNumber, String password, String email, Role role)
    {
        return switch (type)
        {
            case ADMIN -> new Admin(name, phoneNumber, password, email, role);
            case BUYER -> new Buyer(name, phoneNumber, password, email, role);
            case SELLER -> new Seller(name, phoneNumber, password, email, role);
            case COURIER -> new Courier(name, phoneNumber, password, email, role);
        };
    }
}