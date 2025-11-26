package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

public class AdminCreateRepository {

    private final ApiService api;

    public AdminCreateRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void createAdmin(String username, String email, String password, String phone, final CallbackResult<String> cb) {
        Map<String, String> body = Map.of(
                "username", username,
                "email", email,
                "password", password,
                "phoneNumber", phone
        );

        api.createAdmin(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object success = response.body().get("success");
                    Object msg = response.body().get("message");

                    if (success instanceof Boolean && (Boolean) success) {
                        cb.onSuccess(msg != null ? msg.toString() : "Tạo quản trị viên thành công");
                    } else {
                        cb.onError(msg != null ? msg.toString() : "Tạo quản trị viên thất bại");
                    }
                } else {
                    cb.onError("Quyền chỉ dành cho quản trị viên cấp cao");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
