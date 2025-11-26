package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class ShortRepository {
    private final ApiService apiService;

    public ShortRepository(Context context, String baseUrl) {
        apiService = ApiClient.getClient(context, baseUrl).create(ApiService.class);
    }

    /**
     * Lấy 1 video ngẫu nhiên từ server
     */
    public LiveData<Tour> getRandomShort() {
        MutableLiveData<Tour> data = new MutableLiveData<>();

        apiService.getRandomShort().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Tour>> call,
                                   @NonNull Response<ApiResponse<Tour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Tour>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });

        return data;
    }
}