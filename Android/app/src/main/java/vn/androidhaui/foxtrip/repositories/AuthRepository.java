package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AuthRepository {
    private final ApiService api;
    private final SharedPreferences prefs;

    public AuthRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private void saveToken(String token) {
        if (token == null) return;
        prefs.edit().putString("jwt_token", token).apply();
    }

    private String getToken() {
        return prefs.getString("jwt_token", null);
    }

    public void clearToken() {
        prefs.edit().remove("jwt_token").apply();
    }

    private boolean parseBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0;
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return false;
    }

    private User parseUserFromData(Map<String, Object> data) {
        if (data == null) return null;

        String id = data.get("id") != null ? String.valueOf(data.get("id")) : null;
        String username = data.get("username") != null ? String.valueOf(data.get("username")) : null;
        String email = data.get("email") != null ? String.valueOf(data.get("email")) : null;
        String avatar = data.get("avatar") != null ? String.valueOf(data.get("avatar")) : null;

        String token = data.get("token") != null ? String.valueOf(data.get("token")) : null;
        if (token != null) {
            saveToken(token);
        }

        boolean isAdmin = parseBoolean(data.get("admin"));
        boolean isSuperAdmin = parseBoolean(data.get("superadmin"));

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setAdmin(isAdmin);
        user.setSuperAdmin(isSuperAdmin);
        user.setAvatar(avatar);

        return user;
    }

    // Login thông thường
    public void login(String email, String password, CallbackResult<User> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = parseUserFromData(response.body().getData());
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError(response.body() != null ? response.body().getMessage() : "Đăng nhập thất bại");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    //Login with Google
    public void loginWithGoogle(String idToken, CallbackResult<User> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        api.loginWithGoogle(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = parseUserFromData(response.body().getData());
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError(response.body() != null ? response.body().getMessage() : "Đăng nhập Google thất bại");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Login with Facebook
    public void loginWithFacebook(String accessToken, String userID, CallbackResult<User> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("accessToken", accessToken);
        body.put("userID", userID);

        api.loginWithFacebook(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = parseUserFromData(response.body().getData());
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError(response.body() != null ? response.body().getMessage() : "Đăng nhập Facebook thất bại");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Register
    public void register(String username, String email, String password,
                         String phone, String address, CallbackResult<String> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        if (phone != null) body.put("phoneNumber", phone);
        if (address != null) body.put("address", address);

        api.register(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess("Đăng ký thành công");
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Đăng ký thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Logout
    public void logout(CallbackResult<String> cb) {
        String token = getToken();
        if (token == null) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        api.logout().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                clearToken();
                cb.onSuccess("Đã đăng xuất");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Check login status
    public void checkLoginStatus(CallbackResult<User> cb) {
        String token = getToken();
        if (token == null) {
            cb.onError("Chưa đăng nhập");
            return;
        }
        api.checkLoginStatus().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = parseUserFromData(response.body().getData());
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError("Chưa đăng nhập");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError("Lỗi mạng");
            }
        });
    }
}