package com.aut.shoomal.dao;

import com.aut.shoomal.entity.cart.Cart;
import com.aut.shoomal.entity.user.User;

public interface CartDao extends GenericDao<Cart> {
    Cart findByUserId(Long userId);
    Cart findByUser(User user);
}