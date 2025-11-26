package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.SearchOrderResponse;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.models.AdminOrderResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

public class AdminOrderRepository {
    private final ApiService api;

    public AdminOrderRepository(@NonNull Context context, @NonNull String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchOrderDetail(String orderId, final CallbackResult<Order> cb) {
        api.getAdminOrderDetail(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải chi tiết đơn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Log.e("AdminOrderRepo", "fetchOrderDetail", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void fetchOrdersByType(String type, final CallbackResult<List<Order>> cb) {
        Call<ApiResponse<List<Order>>> call;
        switch (type) {
            case "pending":
                call = api.getOrdersPendingPayment();
                break;
            case "to_confirm":
                call = api.getOrdersToConfirm();
                break;
            case "completed":
                call = api.getOrdersCompleted();
                break;
            default:
                cb.onError("Loại đơn không hợp lệ");
                return;
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải danh sách đơn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Log.e("AdminOrderRepo", "fetchOrdersByType", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void searchOrderById(String orderId, final CallbackResult<SearchOrderResponse> cb) {
        api.searchOrderById(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<SearchOrderResponse>> call, Response<ApiResponse<SearchOrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tìm kiếm");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SearchOrderResponse>> call, Throwable t) {
                Log.e("AdminOrderRepo", "searchOrderById", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void deletePendingOrder(String orderId, final CallbackResult<AdminOrderResponse> cb) {
        api.deletePendingOrder(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminOrderResponse>> call, Response<ApiResponse<AdminOrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi xóa đơn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminOrderResponse>> call, Throwable t) {
                Log.e("AdminOrderRepo", "deletePendingOrder", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void confirmOrder(String orderId, final CallbackResult<AdminOrderResponse> cb) {
        api.confirmOrder(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminOrderResponse>> call, Response<ApiResponse<AdminOrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi xác nhận đơn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminOrderResponse>> call, Throwable t) {
                Log.e("AdminOrderRepo", "confirmOrder", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void completeExpiredOrder(String orderId, final CallbackResult<AdminOrderResponse> cb) {
        api.completeExpiredOrder(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminOrderResponse>> call, Response<ApiResponse<AdminOrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi hoàn tất đơn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminOrderResponse>> call, Throwable t) {
                Log.e("AdminOrderRepo", "completeExpiredOrder", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}
