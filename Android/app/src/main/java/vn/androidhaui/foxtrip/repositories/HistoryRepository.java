package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.History;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class HistoryRepository {
    private final ApiService api;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public HistoryRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public void getHistory(final CallbackResult<List<History>> cb) {
        api.getHistory().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<History>>> call, @NonNull Response<ApiResponse<List<History>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tải lịch sử";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<History>>> call, @NonNull Throwable t) {
                Log.e("HistoryRepo", "getHistory onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void getHistoryDetail(String historyId, final CallbackResult<History> cb) {
        api.getHistoryDetail(historyId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<History>> call, @NonNull Response<ApiResponse<History>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tải chi tiết lịch sử";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<History>> call, @NonNull Throwable t) {
                Log.e("HistoryRepo", "getHistoryDetail onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}
