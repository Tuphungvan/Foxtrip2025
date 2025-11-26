package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class OrderRepository {
    private final ApiService api;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public OrderRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public void getMyOrders(final CallbackResult<List<Order>> cb) {
        api.getMyOrders().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tải đơn hàng";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                Log.e("OrderRepo", "getMyOrders onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void getOrderDetail(String orderId, final CallbackResult<Order> cb) {
        api.getOrderDetail(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tải chi tiết đơn hàng";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                Log.e("OrderRepo", "getOrderDetail onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Thêm vào OrderRepository.java

    public void cancelOrder(String orderId, final CallbackResult<String> cb) {
        api.cancelOrder(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String msg = response.body().getMessage() != null ?
                            response.body().getMessage() : "Đã hủy đơn hàng";
                    cb.onSuccess(msg);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi hủy đơn hàng";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e("OrderRepo", "cancelOrder onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void payOrder(String orderId, final CallbackResult<String> cb) {
        api.payOrder(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String msg = response.body().getMessage() != null ?
                            response.body().getMessage() : "Thanh toán thành công";
                    cb.onSuccess(msg);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Số dư ví không đủ";
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e("OrderRepo", "payOrder onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}
