package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class UpdateProfileRepository {
    private final ApiService api;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public UpdateProfileRepository(Context ctx, String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(ctx, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public void getUpdateProfile(CallbackResult<User> cb) {
        api.getUpdateProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    User u = new User();
                    u.setId(data.get("_id") == null ? null : String.valueOf(data.get("_id")));
                    u.setUsername(data.get("username") == null ? null : String.valueOf(data.get("username")));
                    u.setEmail(data.get("email") == null ? null : String.valueOf(data.get("email")));
                    u.setPhoneNumber(data.get("phoneNumber") == null ? null : String.valueOf(data.get("phoneNumber")));
                    u.setAddress(data.get("address") == null ? null : String.valueOf(data.get("address")));
                    cb.onSuccess(u);
                } else {
                    cb.onError("Lỗi tải thông tin");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void postUpdateProfile(Map<String, Object> body, CallbackResult<Void> cb) {
        api.postUpdateProfile(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Cập nhật thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
