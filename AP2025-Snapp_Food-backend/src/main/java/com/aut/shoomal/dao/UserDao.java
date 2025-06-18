package com.aut.shoomal.dao;
import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.exceptions.NotFoundException;

public interface UserDao extends GenericDao<User>
{
    User findByEmail(String email) throws NotFoundException;
    User findByPhoneNumber(String phoneNumber) throws NotFoundException;
    User findByUsername(String username) throws NotFoundException;
    void updateApprovalStatusC(Long id, boolean approved);
}