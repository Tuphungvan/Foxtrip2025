package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AccountRepository {
    private final ApiService api;

    public AccountRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getProfile(final CallbackResult<User> cb) {
        api.getProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null) {
                        User u = new User();
                        u.setId(data.get("id") != null ? String.valueOf(data.get("id")) : (data.get("_id") != null ? String.valueOf(data.get("_id")) : null));
                        u.setUsername(data.get("username") != null ? String.valueOf(data.get("username")) : null);
                        u.setEmail(data.get("email") != null ? String.valueOf(data.get("email")) : null);
                        u.setAvatar(data.get("avatar") != null ? String.valueOf(data.get("avatar")) : null);

                        Object adminObj = data.get("admin");
                        boolean isAdmin = false;
                        if (adminObj instanceof Boolean) isAdmin = (Boolean) adminObj;
                        else if (adminObj instanceof Number) isAdmin = ((Number) adminObj).intValue() != 0;
                        else if (adminObj instanceof String) isAdmin = Boolean.parseBoolean((String) adminObj);
                        u.setAdmin(isAdmin);

                        cb.onSuccess(u);
                        return;
                    }
                }
                cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải profile");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e("ProfileRepo", "getProfile onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void uploadAvatar(MultipartBody.Part avatar, CallbackResult<String> cb) {
        api.uploadAvatar(avatar).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    String url = data != null && data.get("avatar") != null ? String.valueOf(data.get("avatar")) : null;
                    cb.onSuccess(url);
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi upload avatar");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}
