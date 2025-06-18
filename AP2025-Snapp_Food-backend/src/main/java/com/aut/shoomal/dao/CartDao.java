package com.aut.shoomal.dao;

import com.aut.shoomal.Mamad.cart.Cart;
import com.aut.shoomal.Erfan.User;

public interface CartDao extends GenericDao<Cart> {
    Cart findByUserId(Long userId);
    Cart findByUser(User user);
}