// SendOTPRequest.java
package vn.androidhaui.foxtrip.models;

public class SendOTPRequest {
    private String email;
    private String username;

    public SendOTPRequest(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() { return email; }
    public String getUsername() { return username; }
}