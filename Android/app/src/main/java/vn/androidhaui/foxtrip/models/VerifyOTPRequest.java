// VerifyOTPRequest.java
package vn.androidhaui.foxtrip.models;

public class VerifyOTPRequest {
    private String email;
    private String code;

    public VerifyOTPRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() { return email; }
    public String getCode() { return code; }
}