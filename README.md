# Shoomal Food - Online Food Delivery & Shopping App

Shoomal Food is a full-featured online food delivery and shopping application built with **JavaFX** (frontend), **Hibernate** (ORM), and **MySQL** (database). It provides specialized dashboards for buyers, sellers, couriers, and admins, offering a smooth experience for all users.

---

## Key Features

### 🛒 Buyer Dashboard
- Browse restaurants & food items  
- Add items to cart and checkout  
- Track orders & delivery status  
- View order history and transactions  

### 🍽️ Seller Dashboard
- Add, edit, and manage food items  
- Process orders & view sales analytics  

### 🚚 Courier Dashboard
- Accept & manage delivery assignments  
- Update order status in real-time  

### 👑 Admin Dashboard
- Manage users, restaurants, and platform settings  
- Monitor transactions & system performance  

---

## Core Functionalities

### 🛍️ Online Shopping & Food Delivery
- Browse menus, add items to cart, and place orders  

### 💬 Share Comments & Images
- Leave reviews & upload food photos  

### 💰 Internal Wallet
- Secure in-app payments & refunds  

### 📊 Order & Transaction History
- Track past orders & financial transactions  

---

## Technologies Used
- **Frontend:** JavaFX  
- **Backend:** Java 
- **ORM:** Hibernate  
- **Database:** MySQL  

---

## Developers
- **Erfan Rajabi**  
- **Mohammad Musavi**  

---

## Project Structure

The project is divided into two primary modules according to the movi1444/ap2025-shoomal-food file:
*   **Backend (`AP2025-Shoomal_Food-backend`):** Contains the server-side logic, Hibernate entities, DAO layers, JWT authentication, and HTTP request/response handlers.
*   **Frontend (`AP2025-Shoomal_Food-frontend`):** Contains the JavaFX client application, including FXML views, CSS styles, UI controllers, and API service integrators[cite: 1].

```text
AP2025-Shoomal-Food/
│
├── AP2025-Shoomal_Food-backend/
│   ├── src/main/java/com/aut/shoomal/
│   │   ├── auth/                      # Authentication & JWT Management
│   │   ├── dao/                       # Data Access Objects (Hibernate)
│   │   ├── dto/                       # Data Transfer Objects & HTTP Handlers
│   │   ├── entity/                    # Database Entities (User, Food, Cart, etc.)
│   │   ├── exceptions/                # Custom Application Exceptions
│   │   ├── payment/                   # Payment, Wallet, Coupon, and Order Logic
│   │   ├── rating/                    # Rating & Review Management
│   │   └── util/                      # Utilities (HibernateUtil, JwtUtil)
│   └── src/main/resources/            # Backend Configurations (Hibernate, Logback)
│
└── AP2025-Shoomal_Food-frontend/
    ├── src/main/java/com/aut/shoomal/
    │   ├── controllers/               # JavaFX FXML Controllers
    │   ├── dto/                       # Client-side Data Transfer Objects
    │   ├── service/                   # API Communication Services
    │   └── utils/                     # Client Utilities (e.g., ImageToBase64Converter)
    └── src/main/resources/com/aut/shoomal/
        ├── fonts/                     # UI Fonts (Vazirmatn, Sahel, IranNastaliq, Titr)
        ├── images/                    # Application Assets & Icons
        ├── styles/                    # JavaFX CSS Stylesheets
        └── views/                     # JavaFX FXML Layouts & Components
