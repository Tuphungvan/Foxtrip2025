package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.models.CheckoutData;
import vn.androidhaui.foxtrip.models.OrderResponse;
import vn.androidhaui.foxtrip.models.PaymentData;
import vn.androidhaui.foxtrip.models.PlaceOrderRequest;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

public class CheckoutRepository {
    private final ApiService apiService;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<String> getMessage() {
        return message;
    }

    public CheckoutRepository(Context context, String baseUrl) {
        apiService = ApiClient.getClient(context, baseUrl).create(ApiService.class);
    }

    public LiveData<CheckoutData> getCheckout() {
        MutableLiveData<CheckoutData> data = new MutableLiveData<>();
        apiService.getCheckout().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CheckoutData>> call, @NonNull Response<ApiResponse<CheckoutData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    data.postValue(response.body().getData());
                } else data.postValue(null);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CheckoutData>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    public LiveData<OrderResponse> placeOrder(String username, String phoneNumber, String address, String email) {
        MutableLiveData<OrderResponse> result = new MutableLiveData<>();
        PlaceOrderRequest body = new PlaceOrderRequest(username, phoneNumber, address, email);

        apiService.placeOrder(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call,
                                   @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OrderResponse> apiRes = response.body();

                    if (apiRes.isSuccess()) {
                        result.postValue(apiRes.getData());
                    } else {
                        // ❗ Backend báo lỗi (VD: tour không còn khả dụng)
                        message.postValue(apiRes.getMessage());
                        result.postValue(null);
                    }
                } else {
                    try {
                        // Nếu backend trả lỗi 400, đọc message thủ công
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            org.json.JSONObject json = new org.json.JSONObject(errorJson);
                            String msg = json.optString("message", "Đặt hàng thất bại");
                            message.postValue(msg);
                        }
                    } catch (Exception e) {
                        message.postValue("Đặt hàng thất bại");
                    }
                    result.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                message.postValue("Lỗi kết nối máy chủ");
                result.postValue(null);
            }
        });
        return result;
    }


    public LiveData<PaymentData> getPaymentInfo(String orderId) {
        MutableLiveData<PaymentData> data = new MutableLiveData<>();
        apiService.getPaymentInfo(orderId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaymentData>> call, @NonNull Response<ApiResponse<PaymentData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    data.postValue(response.body().getData());
                } else data.postValue(null);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PaymentData>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }
}
