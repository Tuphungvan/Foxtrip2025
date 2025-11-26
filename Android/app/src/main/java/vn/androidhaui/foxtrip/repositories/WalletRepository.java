package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

import java.util.Map;
import java.util.HashMap;

public class WalletRepository {
    private final ApiService api;

    public interface WalletCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public WalletRepository(Context context) {
        api = ApiClient.getClient(context, context.getString(R.string.base_url)).create(ApiService.class);
    }

    public void getWallet(WalletCallback<Map<String, Object>> callback) {
        api.getWallet().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Lỗi khi lấy số dư");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void recharge(String amount, WalletCallback<Map<String, Object>> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);

        api.postRechargeWallet(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Nạp tiền thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
