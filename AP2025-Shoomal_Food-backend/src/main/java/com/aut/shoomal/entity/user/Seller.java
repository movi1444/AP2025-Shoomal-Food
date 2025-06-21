package com.aut.shoomal.entity.user;
import com.aut.shoomal.entity.user.access.Role;
import com.aut.shoomal.entity.restaurant.Restaurant;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue(value = "SELLER")
public class Seller extends User
{
    @Column(name = "restaurantInfo", columnDefinition = "TEXT")
    private String additionalRestaurantInfo;
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Restaurant restaurant;

    public Seller() {}
    public Seller(String name, String phoneNumber, String password, String email, Role role)
    {
        super(name, phoneNumber, password, email, role);
    }

    public String getAdditionalRestaurantInfo()
    {
        return this.additionalRestaurantInfo;
    }

    public void setAdditionalRestaurantInfo(String additionalRestaurantInfo)
    {
        this.additionalRestaurantInfo = additionalRestaurantInfo;
    }

    public Restaurant getRestaurant()
    {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant)
    {
        this.restaurant = restaurant;
    }
}
