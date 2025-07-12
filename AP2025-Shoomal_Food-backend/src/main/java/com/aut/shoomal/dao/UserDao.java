package com.aut.shoomal.dao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;

public interface UserDao extends GenericDao<User>
{
    User findByEmail(String email) throws NotFoundException;
    User findByPhoneNumber(String phoneNumber) throws NotFoundException;
    User findByUsername(String username) throws NotFoundException;
    void updateApprovalStatus(Long id, boolean approved);
}