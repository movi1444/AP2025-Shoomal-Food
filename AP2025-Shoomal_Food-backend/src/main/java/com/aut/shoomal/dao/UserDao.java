package com.aut.shoomal.dao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;

import java.util.List;

public interface UserDao extends GenericDao<User>
{
    User findByEmail(String email) throws NotFoundException;
    User findByPhoneNumber(String phoneNumber) throws NotFoundException;
    User findByUsername(String username) throws NotFoundException;
    List<User> findCustomersWithOrder(Session session, Long restaurantId);
    List<User> findCouriersWithOrder(Session session, Long restaurantId);
    void updateApprovalStatus(Long id, boolean approved);
}