package com.aut.shoomal.dao;

import com.aut.shoomal.rating.Rating;
import org.hibernate.Session;

import java.util.List;

public interface RatingDao extends GenericDao<Rating>
{
    List<Rating> checkConflict(Session session, Long userId, Long foodId, Integer orderId);
    List<Rating> getByOrderId(Session session, Long userId, Integer orderId);
}