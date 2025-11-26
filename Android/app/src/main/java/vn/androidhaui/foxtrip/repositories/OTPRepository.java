package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.SendOTPRequest;
import vn.androidhaui.foxtrip.models.VerifyOTPRequest;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class OTPRepository {
    private final ApiService apiService;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<String> getMessage() {
        return message;
    }

    public OTPRepository(Context context, String baseUrl) {
        apiService = ApiClient.getClient(context, baseUrl).create(ApiService.class);
    }

    public LiveData<Boolean> sendOTP(String email, String username) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        SendOTPRequest request = new SendOTPRequest(email, username);

        apiService.sendOTP(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        message.postValue(response.body().getMessage());
                        result.postValue(true);
                    } else {
                        message.postValue(response.body().getMessage());
                        result.postValue(false);
                    }
                } else {
                    message.postValue("Không thể gửi OTP");
                    result.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                message.postValue("Lỗi kết nối máy chủ");
                result.postValue(false);
            }
        });
        return result;
    }

    public LiveData<Boolean> verifyOTP(String email, String code) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        VerifyOTPRequest request = new VerifyOTPRequest(email, code);

        apiService.verifyOTP(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        message.postValue(response.body().getMessage());
                        result.postValue(true);
                    } else {
                        message.postValue(response.body().getMessage());
                        result.postValue(false);
                    }
                } else {
                    message.postValue("Mã OTP không đúng");
                    result.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                message.postValue("Lỗi kết nối máy chủ");
                result.postValue(false);
            }
        });
        return result;
    }
}