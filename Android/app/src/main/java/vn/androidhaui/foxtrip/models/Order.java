package vn.androidhaui.foxtrip.models;

import java.util.List;

public class Order {
    private String _id;
    private String userId;
    private CustomerInfo customerInfo;   // thêm
    private List<OrderItem> items;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String createdAt;            // thêm (ISO string từ backend)
    private String updatedAt;

    public String getId() { return _id; }
    public String getUserId() { return userId; }
    public CustomerInfo getCustomerInfo() { return customerInfo; }
    public List<OrderItem> getItems() { return items; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void set_id(String _id) { this._id = _id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCustomerInfo(CustomerInfo customerInfo) { this.customerInfo = customerInfo; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

}