package vn.androidhaui.foxtrip.models;

public class Wallet {
    private String userId;
    private Double balance;

    public String getUserId() { return userId; }
    public Double getBalance() { return balance != null ? balance : 0.0; }
}
