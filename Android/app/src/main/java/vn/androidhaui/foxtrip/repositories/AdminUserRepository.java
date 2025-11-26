package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AdminUserRepository {
    private final ApiService api;

    public AdminUserRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchUsers(String search, String sort, final CallbackResult<List<User>> cb) {
        api.getAdminUsers(search, sort).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải danh sách người dùng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void deactivateUser(String id, final CallbackResult<User> cb) {
        api.deactivateAdminUser(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi khoá người dùng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void activateUser(String id, final CallbackResult<User> cb) {
        api.activateAdminUser(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi kích hoạt người dùng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void resetPassword(String id, final CallbackResult<User> cb) {
        api.resetAdminUserPassword(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi reset mật khẩu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
