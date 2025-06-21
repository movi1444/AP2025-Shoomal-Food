package com.aut.shoomal.rating;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.RatingDao;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import org.hibernate.Session;

public class RatingManager
{
    private final RatingDao ratingDao;
    private final OrderManager orderManager;
    private final UserManager userManager;
    public RatingManager(RatingDao ratingDao, OrderManager orderManager, UserManager userManager)
    {
        this.ratingDao = ratingDao;
        this.orderManager = orderManager;
        this.userManager = userManager;
    }

    public void addRating(Rating rating)
    {
        ratingDao.create(rating);
    }

    public void addRating(Rating rating, Session session)
    {
        ratingDao.create(rating, session);
    }

    public void deleteRating(Integer ratingId)
    {
        ratingDao.delete(Long.valueOf(ratingId));
    }

    public void updateRating(Rating rating)
    {
        ratingDao.update(rating);
    }

    public void updateRating(Rating rating, Session session)
    {
        ratingDao.update(rating, session);
    }

    public Rating submitRating(Integer orderId, Integer ratingNumber, Long userId, String comment, String image)
    {
        try {
            Order order = orderManager.findOrderById(orderId);
            User user = userManager.getUserById(userId);
            if (order == null)
                throw new NotFoundException("Order with id " + orderId + " not found.");
            if (user == null)
                throw new NotFoundException("User with id " + userId + " not found.");

            if (ratingNumber == null)
                throw new InvalidInputException("Rating number required.");
            if (ratingNumber < 1 || ratingNumber > 5)
                throw new InvalidInputException("Rating number out of range.");
            if (comment == null || comment.trim().isEmpty())
                throw new InvalidInputException("Comment required.");

            return new Rating(
                    order,
                    (image != null && !image.trim().isEmpty()) ? image : null,
                    comment,
                    ratingNumber,
                    user
            );
        } catch (InvalidInputException e) {
            System.err.println("Rating submission failed dou to invalid input: " + e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            System.err.println("Rating submission failed due to missing resource: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Rating submission failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit rating: " + e.getMessage(), e);
        }
    }
}