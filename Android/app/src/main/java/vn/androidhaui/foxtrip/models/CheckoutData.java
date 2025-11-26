package vn.androidhaui.foxtrip.models;

public class CheckoutData {
    private Cart cart;
    private Double total;
    private User user;

    public Cart getCart() { return cart; }
    public Double getTotal() { return total != null ? total : 0.0; }
    public User getUser() { return user; }
}

