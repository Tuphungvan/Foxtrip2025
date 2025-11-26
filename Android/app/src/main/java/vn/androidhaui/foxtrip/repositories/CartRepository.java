package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.CartData;
import vn.androidhaui.foxtrip.models.CountData;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class CartRepository {
    private final ApiService apiService;

    public CartRepository(Context context, String baseUrl) {
        apiService = ApiClient.getClient(context, baseUrl).create(ApiService.class);
    }

    public LiveData<CartData> getCart() {
        MutableLiveData<CartData> data = new MutableLiveData<>();
        apiService.getCart().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartData>> call, @NonNull Response<ApiResponse<CartData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    data.postValue(response.body().getData());
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartData>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    public LiveData<String> removeFromCart(String slug) {
        MutableLiveData<String> result = new MutableLiveData<>();
        apiService.removeFromCart(slug).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartData>> call, @NonNull Response<ApiResponse<CartData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) result.postValue("success");
                    else result.postValue(response.body().getMessage());
                } else {
                    result.postValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartData>> call, @NonNull Throwable t) {
                result.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
        return result;
    }

    public LiveData<String> increase(String slug) {
        MutableLiveData<String> result = new MutableLiveData<>();
        apiService.increaseQuantity(slug).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartData>> call, @NonNull Response<ApiResponse<CartData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) result.postValue("success");
                    else result.postValue(response.body().getMessage());
                } else {
                    result.postValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartData>> call, @NonNull Throwable t) {
                result.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
        return result;
    }

    public LiveData<String> decrease(String slug) {
        MutableLiveData<String> result = new MutableLiveData<>();
        apiService.decreaseQuantity(slug).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartData>> call, @NonNull Response<ApiResponse<CartData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) result.postValue("success");
                    else result.postValue(response.body().getMessage());
                } else {
                    result.postValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartData>> call, @NonNull Throwable t) {
                result.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
        return result;
    }

    public LiveData<Integer> getCount() {
        MutableLiveData<Integer> count = new MutableLiveData<>(0);
        apiService.getCartCount().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CountData>> call, @NonNull Response<ApiResponse<CountData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    count.postValue(response.body().getData() != null ? response.body().getData().getCount() : 0);
                } else {
                    count.postValue(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CountData>> call, @NonNull Throwable t) {
                count.postValue(0);
            }
        });
        return count;
    }
}
