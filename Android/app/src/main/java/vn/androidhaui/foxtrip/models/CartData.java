package vn.androidhaui.foxtrip.models;

public class CartData {

    private Cart cart;
    private Double total;

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Double getTotal() { return total != null ? total : null; }
    public void setTotal(Double total) { this.total = total; }
}
