package com.aut.shoomal.rating;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.RatingDao;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

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

    public void deleteRating(Session session, Integer ratingId)
    {
        Rating rating = session.get(Rating.class, ratingId);
        if (rating == null)
            throw new NotFoundException("Rating with id " + ratingId + " not found.");
        session.remove(rating);
    }

    public void updateRating(Session session, Integer id, Integer ratingNumber, String comment, List<String> images)
    {
        Transaction transaction = null;
        try {
            Rating rating = session.get(Rating.class, id);
            transaction = session.beginTransaction();
            if (ratingNumber != null)
            {
                if (ratingNumber > 5 || ratingNumber < 1)
                    throw new InvalidInputException("Rating number out of range.");
                rating.setRating(ratingNumber);
            }

            if (comment != null)
                rating.setComment(comment.trim());
            if (images != null)
                rating.setImageBase64(images);
            else if (rating.getImageBase64() != null)
                rating.setImageBase64(null);

            ratingDao.update(rating, session);
            transaction.commit();
        } catch (InvalidInputException e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw new RuntimeException("Update rating failed: " + e.getMessage(), e);
        }
    }

    public Rating submitRating(Integer orderId, Integer ratingNumber, Long userId, String comment, List<String> image)
    {
        try {
            if (orderId == null)
                throw new InvalidInputException("orderId required.");
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
                    image,
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

    public boolean checkConflict(Session session, Long userId, Long foodId, Integer orderId)
    {
        List<Rating> ratings = ratingDao.checkConflict(session, userId, foodId, orderId);
        return ratings != null && !ratings.isEmpty();
    }

    public List<Rating> getByOrderId(Session session, Long userId, Integer orderId)
    {
        return ratingDao.getByOrderId(session, userId, orderId);
    }
}