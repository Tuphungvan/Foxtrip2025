package vn.androidhaui.foxtrip.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class ChatRepository {

    private final ApiService apiService;

    public ChatRepository(@NonNull Context context, @NonNull String baseUrl) {
        apiService = ApiClient.getClient(context, baseUrl).create(ApiService.class);
    }

    public interface ChatCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public void sendMessage(Map<String, String> body, ChatCallback callback) {
        apiService.sendChatMessage(body).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call,
                                   @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    String msg = "Lỗi server";

                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            msg = "Lỗi phản hồi từ server";
                        }
                    }
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
