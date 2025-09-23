package com.aut.shoomal.entity.restaurant;

import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.dto.request.CreateRestaurantRequest;
import com.aut.shoomal.dto.request.UpdateRestaurantRequest;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.user.User;
import org.hibernate.Session;

import java.util.List;

public class RestaurantManager {

    private final RestaurantDao restaurantDao;
    private final UserDao userDao;

    public RestaurantManager(RestaurantDao restaurantDao, UserDao userDao) {
        this.restaurantDao = restaurantDao;
        this.userDao = userDao;
    }

    public boolean isOwner(int restaurantId, String userId) {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            return false;
        }
        User ownerUser = userDao.findById(Long.parseLong(userId));
        if (ownerUser == null) {
            return false;
        }

        return restaurant.getOwner() != null && restaurant.getOwner().getId().equals(ownerUser.getId());
    }

    public List<Restaurant> findByCourier(Session session, Long courierId) {
        return restaurantDao.findByCourier(session, courierId);
    }

    public Restaurant createRestaurant(CreateRestaurantRequest request, String userId) throws InvalidInputException, ConflictException, ForbiddenException, NotFoundException {
        if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getAddress() == null || request.getAddress().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new InvalidInputException("Name, address, and phone are required.");
        }

        Seller owner = (Seller) userDao.findById(Long.parseLong(userId));
        if (owner == null) {
            throw new NotFoundException("Seller not found for the given user ID.");
        }

        List<Restaurant> existingRestaurants = restaurantDao.findByOwner(owner);
        if (existingRestaurants != null && !existingRestaurants.isEmpty()) {
            throw new ConflictException("This seller already owns a restaurant. Each seller can only have one restaurant.");
        }

        if (restaurantDao.findByName(request.getName()) != null) {
            throw new ConflictException("Restaurant with this name already exists.");
        }

        Restaurant newRestaurant = new Restaurant();
        newRestaurant.setName(request.getName());
        newRestaurant.setPhone(request.getPhone());
        newRestaurant.setAddress(request.getAddress());
        newRestaurant.setLogoBase64(request.getLogoBase64());
        newRestaurant.setTaxFee(request.getTaxFee() != null ? request.getTaxFee() : 0);
        newRestaurant.setAdditionalFee(request.getAdditionalFee() != null ? request.getAdditionalFee() : 0);
        newRestaurant.setOwner(owner);
        newRestaurant.setWorkingHours("Not set");
        newRestaurant.setDescription("No description provided.");

        restaurantDao.create(newRestaurant);
        return newRestaurant;
    }

    public List<Restaurant> getRestaurantsBySeller(String userId) throws NotFoundException {
        Seller seller = (Seller) userDao.findById(Long.parseLong(userId));
        if (seller == null) {
            throw new NotFoundException("Seller not found.");
        }
        return restaurantDao.findByOwner(seller);
    }

    public Restaurant updateRestaurant(int restaurantId, UpdateRestaurantRequest request, String userId) throws NotFoundException, InvalidInputException, ForbiddenException {
        Restaurant existingRestaurant = restaurantDao.findById((long) restaurantId);
        if (existingRestaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to update this restaurant.");
        }

        if (request.getName() != null) existingRestaurant.setName(request.getName());
        if (request.getAddress() != null) existingRestaurant.setAddress(request.getAddress());
        if (request.getPhone() != null) existingRestaurant.setPhone(request.getPhone());
        if (request.getLogoBase64() != null) existingRestaurant.setLogoBase64(request.getLogoBase64());
        if (request.getTaxFee() != null) {
            if (request.getTaxFee() < 0)
                throw new InvalidInputException("Tax fee cannot be negative.");
            existingRestaurant.setTaxFee(request.getTaxFee());
        }
        if (request.getAdditionalFee() != null) {
            if (request.getAdditionalFee() < 0)
                throw new InvalidInputException("Additional fee cannot be negative.");
            existingRestaurant.setAdditionalFee(request.getAdditionalFee());
        }

        restaurantDao.update(existingRestaurant);
        return existingRestaurant;
    }

    public List<Restaurant> getAllApprovedRestaurants() {
        return restaurantDao.findAll();
    }

    public List<Restaurant> searchRestaurantByName(String restaurantName) {
        return restaurantDao.searchByName(restaurantName);
    }

    public Restaurant findById(Long restaurantId) {
        return restaurantDao.findById(restaurantId);
    }
}