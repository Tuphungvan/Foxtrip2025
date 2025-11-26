package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class TourRepository {
    private final ApiService apiService;

    public TourRepository(Context context, String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        apiService = retrofit.create(ApiService.class);
    }

    public LiveData<Tour> getTourDetail(String slug) {
        MutableLiveData<Tour> data = new MutableLiveData<>();
        apiService.getTourDetail(slug).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Tour>> call, @NonNull Response<ApiResponse<Tour>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    data.postValue(response.body().getData());
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Tour>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    public LiveData<String> addToCart(String slug, int quantity) {
        MutableLiveData<String> result = new MutableLiveData<>();
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", quantity);

        apiService.addToCart(slug, body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        result.postValue("success");
                    } else {
                        result.postValue(response.body().getMessage());
                    }
                } else {
                    result.postValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.postValue("Lỗi mạng: " + t.getMessage());
            }
        });

        return result;
    }
}
