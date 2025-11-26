package vn.androidhaui.foxtrip.models;

import java.util.List;

public class SearchOrderResponse {
    private List<Order> orders;
    private String status; // Status của order tìm được

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}