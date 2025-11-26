package vn.androidhaui.foxtrip.models;

public class PlaceOrderRequest {
    private String username;
    private String phoneNumber;
    private String address;
    private String email;

    public PlaceOrderRequest(String username, String phoneNumber, String address, String email) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
}
