package com.aut.shoomal.Erfan;
import com.aut.shoomal.Erfan.access.Role;

public class UserCreator
{
    public static User createUser(UserTypes type, String name, String phoneNumber, String password, String email, Role role)
    {
        return switch (type)
        {
            case ADMIN -> new Admin(name, phoneNumber, password, email, role);
            case BUYER -> new Buyer(name, phoneNumber, password, email, role);
            case SELLER -> new Seller(name, phoneNumber, password, email, role);
            case COURIER -> new Courier(email, phoneNumber, password, email, role);
        };
    }
}