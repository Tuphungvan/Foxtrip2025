package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class HomeRepository {
    private final ApiService apiService;

    public HomeRepository(Context context, String baseUrl) {
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        apiService = retrofit.create(ApiService.class);
    }

    private <T> void handleResponse(MutableLiveData<T> out, Response<ApiResponse<T>> response) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> body = response.body();
            android.util.Log.d("API_RESPONSE",
                    "success=" + body.isSuccess() +
                            " message=" + body.getMessage() +
                            " data=" + (body.getData() == null ? "null" : body.getData().toString())
            );

            if (body.isSuccess()) {
                out.postValue(body.getData());
            } else {
                out.postValue(null);
            }
        } else {
            android.util.Log.w("API_RESPONSE", "failed code=" + response.code());
            out.postValue(null);
        }
    }


    private <T> void handleFailure(MutableLiveData<T> out) {
        out.postValue(null);
    }

    public LiveData<List<Tour>> getToursBac() {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.getToursBac().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                android.util.Log.d("API_GET_TOURS_BAC", "status=" + response.code()
                        + " body=" + response.body());
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                android.util.Log.e("API_GET_TOURS_BAC", "error=" + t.getMessage(), t);
                handleFailure(out);
            }
        });
        return out;
    }


    public LiveData<List<Tour>> getToursTrung() {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.getToursTrung().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                handleFailure(out);
            }
        });
        return out;
    }

    public LiveData<List<Tour>> getToursNam() {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.getToursNam().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                handleFailure(out);
            }
        });
        return out;
    }

    public LiveData<List<Tour>> getHotTours() {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.getHotTours().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                handleFailure(out);
            }
        });
        return out;
    }

    public LiveData<List<Tour>> getDiscountTours() {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.getDiscountTours().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                handleFailure(out);
            }
        });
        return out;
    }

    public LiveData<List<Tour>> searchTours(String q) {
        MutableLiveData<List<Tour>> out = new MutableLiveData<>();
        apiService.searchTours( q, null, null, null, null, null, null).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call,
                                   @NonNull Response<ApiResponse<List<Tour>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call,
                                  @NonNull Throwable t) {
                handleFailure(out);
            }
        });
        return out;
    }
}
