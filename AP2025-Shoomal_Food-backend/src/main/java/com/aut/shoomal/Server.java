package com.aut.shoomal;

import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.user.access.RoleManager;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.entity.menu.MenuManager;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.auth.*;
import com.aut.shoomal.dao.*;
import com.aut.shoomal.dao.impl.*;
import com.aut.shoomal.dto.handler.*;
import com.aut.shoomal.payment.order.OrderManager;

import com.aut.shoomal.payment.coupon.CouponManager;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.wallet.WalletManager;
import com.aut.shoomal.rating.RatingManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server
{
    private HttpServer server;
    private final int port;
    public Server(int port)
    {
        this.port = port;
    }

    public void run()
    {
        RoleDao roleDao = new RoleDaoImpl();
        UserDao userDao = new UserDaoImpl();
        BlacklistedTokenDao blacklistedTokenDao = new BlacklistedTokenDaoImpl();
        RestaurantDao restaurantDao = new RestaurantDaoImpl();
        FoodDao foodDao = new FoodDaoImpl();
        MenuDao menuDao = new MenuDaoImpl();
        OrderDao orderDao = new OrderDaoImpl();
        CouponDao couponDao = new CouponDaoImpl();
        RatingDao ratingDao = new RatingDaoImpl();
        TransactionDao transactionDao = new TransactionDaoImpl();
        WalletDao walletDao = new WalletDaoImpl();

        UserManager userManager = new UserManager(userDao);
        RoleManager roleManager = new RoleManager(roleDao);
        SignupManager signupManager = new SignupManager(userManager, roleManager);
        LoginManager loginManager = new LoginManager(userManager);
        LogoutManager logoutManager = new LogoutManager(blacklistedTokenDao);
        RestaurantManager restaurantManager = new RestaurantManager(restaurantDao, foodDao, menuDao, userDao);
        MenuManager menuManager = new MenuManager(menuDao, restaurantManager, restaurantDao, foodDao);
        FoodManager foodManager = new FoodManager(foodDao, restaurantManager, restaurantDao, menuDao, menuManager);
        CouponManager couponManager = new CouponManager(couponDao);
        OrderManager orderManager = new OrderManager(orderDao, userManager, couponManager, restaurantManager, foodManager);
        RatingManager ratingManager = new RatingManager(ratingDao, orderManager, userManager);
        PaymentTransactionManager paymentTransactionManager = new PaymentTransactionManager(transactionDao, orderManager);
        WalletManager walletManager = new WalletManager(walletDao, paymentTransactionManager, orderManager);

        BuyerBrowseHandler buyerBrowseHandler = new BuyerBrowseHandler(userManager, restaurantManager,couponManager, foodManager, blacklistedTokenDao);
        BuyerOrderHandler buyerOrderHandler = new BuyerOrderHandler(userManager, orderManager, walletManager, paymentTransactionManager, blacklistedTokenDao);
        BuyerFavoriteHandler buyerFavoriteHandler = new BuyerFavoriteHandler(userManager, blacklistedTokenDao);
        BuyerRatingHandler buyerRatingHandler = new BuyerRatingHandler(userManager, ratingManager, blacklistedTokenDao);
        AdminHandler adminHandler = new AdminHandler(userManager, restaurantManager, blacklistedTokenDao, orderManager, paymentTransactionManager);
        AdminCouponHandler adminCouponHandler = new AdminCouponHandler(userManager, blacklistedTokenDao, couponManager);
        CourierHandler courierHandler = new CourierHandler(userManager,orderManager,blacklistedTokenDao);

        try {
            //signupManager.ensureAdminUserExists();
            final HttpServer finalServer = HttpServer.create(new InetSocketAddress(port), 0);
            server = finalServer;

            int numberOfCores = Runtime.getRuntime().availableProcessors();
            int numberOfThreads = numberOfCores * 2;
            System.out.println("Available CPU cores: " + numberOfCores);

            finalServer.createContext("/auth/register", new RegisterHandler(signupManager));
            finalServer.createContext("/auth/login", new LoginHandler(loginManager));
            finalServer.createContext("/auth/profile", new ProfileHandler(userManager, blacklistedTokenDao));
            finalServer.createContext("/auth/logout", new LogoutHandler(logoutManager));

            finalServer.createContext("/restaurants", new RestaurantHandler(restaurantManager, foodManager, menuManager, userManager, blacklistedTokenDao, orderManager));

            finalServer.createContext("/vendors", buyerBrowseHandler);
            finalServer.createContext("/items", buyerBrowseHandler);
            finalServer.createContext("/coupons", buyerBrowseHandler);
            finalServer.createContext("/ratings", buyerRatingHandler);

            finalServer.createContext("/orders", buyerOrderHandler);
            finalServer.createContext("/orders/history", buyerOrderHandler);
            finalServer.createContext("/transactions", buyerOrderHandler);
            finalServer.createContext("/wallet/top-up", buyerOrderHandler);
            finalServer.createContext("/payment/online", buyerOrderHandler);

            finalServer.createContext("/favorites", buyerFavoriteHandler);

            finalServer.createContext("/deliveries", courierHandler);

            finalServer.createContext("/admin/users", adminHandler);
            finalServer.createContext("/admin/orders", adminHandler);
            finalServer.createContext("/admin/transactions", adminHandler);
            finalServer.createContext("/admin/coupons", adminCouponHandler);
            //finalServer.createContext("/admin/reports", adminHandler);

            finalServer.setExecutor(Executors.newFixedThreadPool(numberOfThreads));
            finalServer.start();

            System.out.println("Server started on port " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                finalServer.stop(0);
                HibernateUtil.shutdown();
                System.out.println("Server and HibernateUtil shut down.");
            }));
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
            if (server != null)
                server.stop(0);
        }
    }
}