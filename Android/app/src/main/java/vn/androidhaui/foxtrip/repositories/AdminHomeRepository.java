package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.RevenueReport;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AdminHomeRepository {
    private final ApiService api;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public AdminHomeRepository(@NonNull Context ctx, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(ctx, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public void getOverview(CallbackResult<Map<String, Object>> cb) {
        api.getAdminOverview().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải tổng quan");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void getRevenue(CallbackResult<List<RevenueReport>> cb) {
        api.getAdminRevenue().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RevenueReport>>> call,
                                   @NonNull Response<ApiResponse<List<RevenueReport>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải doanh thu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RevenueReport>>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}