package com.aut.shoomal.entity.user;
import com.aut.shoomal.dao.UserDao;
import org.hibernate.Session;

import java.util.List;

public class UserManager
{
    private final UserDao userDao;

    public UserManager(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void addUser(User user)
    {
        userDao.create(user);
    }

    public void updateUser(User user)
    {
        userDao.update(user);
    }

    public User getUserById(Long id)
    {
        return userDao.findById(id);
    }

    public User getUserByEmail(String email)
    {
        return userDao.findByEmail(email);
    }

    public User getUserByPhoneNumber(String phoneNumber)
    {
        return userDao.findByPhoneNumber(phoneNumber);
    }

    public User getUserByName(String username)
    {
        return userDao.findByUsername(username);
    }

    public List<User> getAllUsers()
    {
        return userDao.findAll();
    }

    public void setUserApprovalStatus(String userId, UserStatus userStatus){
        boolean approved = userStatus == UserStatus.APPROVED;
        userDao.updateApprovalStatus(Long.parseLong(userId), approved);
    }

    public List<User> findCustomersWithOrder(Session session, Long restaurantId)
    {
        return userDao.findCustomersWithOrder(session, restaurantId);
    }

    public List<User> findCouriersWithOrder(Session session, Long restaurantId)
    {
        return userDao.findCouriersWithOrder(session, restaurantId);
    }
}