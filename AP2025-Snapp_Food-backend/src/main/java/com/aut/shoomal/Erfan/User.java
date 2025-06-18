package com.aut.shoomal.Erfan;
import com.aut.shoomal.Erfan.access.Role;
import com.aut.shoomal.Mamad.restaurant.Restaurant;
import com.aut.shoomal.auth.SignupManager;
import com.aut.shoomal.payment.Order;
import com.aut.shoomal.rating.Rating;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "userType", discriminatorType = DiscriminatorType.STRING)
public abstract class User
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true, length = 11)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Column(unique = true)
    private String email;
    @Column(nullable = false)
    private String address;
    @Column(name = "profile")
    private String profileImageBase64;
    @Column(nullable = false)
    private String salt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "bank_id", unique = true)
    private BankInfo bank;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Order> customerOrders = new ArrayList<>();

    @OneToMany(mappedBy = "courier", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Order> courierOrders = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_favorite_restaurants",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favorites = new ArrayList<>();

    public User() {}
    public User(String name, String phoneNumber, String password, String email, Role role)
    {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.email = (email != null && email.trim().isEmpty()) ? null : email;
        this.role = role;
    }

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        password = SignupManager.hashPassword(password, this.salt);
        this.password = password;
    }

    public boolean confirmPassword(String password)
    {
        return SignupManager.verifyPassword(password, this.password, this.salt);
    }

    public List<Order> getCustomerOrders()
    {
        return customerOrders;
    }

    public void setCustomerOrders(List<Order> customerOrders)
    {
        this.customerOrders = customerOrders;
    }

    public void addCustomerOrder(Order order)
    {
        this.customerOrders.add(order);
        order.setCustomer(this);
    }

    public void removeCustomerOrder(Order order)
    {
        this.customerOrders.remove(order);
        order.setCustomer(null);
    }

    public List<Order> getCourierOrders()
    {
        return courierOrders;
    }

    public void setCourierOrders(List<Order> courierOrders)
    {
        this.courierOrders = courierOrders;
    }

    public void addCourierOrder(Order order)
    {
        this.courierOrders.add(order);
        order.setCourier(this);
    }

    public void removeCourierOrder(Order order)
    {
        this.courierOrders.remove(order);
        order.setCourier(null);
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(String email)
    {
        this.email = (email != null && email.trim().isEmpty()) ? null : email;
    }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    public BankInfo getBank()
    {
        return bank;
    }

    public void setBank(BankInfo bank)
    {
        this.bank = bank;
        if (bank != null)
            bank.setUser(this);
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getProfileImageBase64()
    {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64)
    {
        this.profileImageBase64 = profileImageBase64;
    }

    public List<Restaurant> getFavorites()
    {
        return favorites;
    }

    public void setFavorites(List<Restaurant> favorites)
    {
        this.favorites = favorites;
    }

    public void addFavorite(Restaurant favorite)
    {
        this.favorites.add(favorite);
        favorite.getUsers().add(this);
    }

    public void removeFavorite(Restaurant favorite)
    {
        this.favorites.remove(favorite);
        favorite.getUsers().remove(this);
    }

    public List<Rating> getRatings()
    {
        return ratings;
    }

    public void setRatings(List<Rating> ratings)
    {
        this.ratings = ratings;
    }

    public void addRating(Rating rating)
    {
        this.ratings.add(rating);
        rating.setUser(this);
    }

    public void deleteRating(Rating rating)
    {
        this.ratings.remove(rating);
        rating.setUser(null);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}