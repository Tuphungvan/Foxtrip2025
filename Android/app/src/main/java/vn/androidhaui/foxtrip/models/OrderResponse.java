package vn.androidhaui.foxtrip.models;

import java.util.List;

public class OrderResponse {
    private List<OrderInfo> orders;
    private int totalOrders;
    private String status;

    public List<OrderInfo> getOrders() { return orders; }
    public int getTotalOrders() { return totalOrders; }
    public String getStatus() { return status; }

    // Inner class cho từng order
    public static class OrderInfo {
        private String orderId;
        private String tourName;
        private String status;

        public String getOrderId() { return orderId; }
        public String getTourName() { return tourName; }
        public String getStatus() { return status; }
    }
}