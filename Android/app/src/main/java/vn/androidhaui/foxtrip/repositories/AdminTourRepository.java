package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AdminTourRepository {
    private final ApiService api;
    private final Context context;

    public AdminTourRepository(@NonNull Context context, @NonNull String baseUrl) {
        this.context = context;
        Retrofit retrofit = ApiClient.getClient(context, baseUrl);
        api = retrofit.create(ApiService.class);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchTours(final CallbackResult<List<Tour>> cb) {
        api.getAdminTours().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải danh sách tour");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                Log.e("AdminTourRepo", "fetchTours", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void createTour(Map<String, Object> body, final CallbackResult<Tour> cb) {
        api.createAdminTour(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<Tour>> call, Response<ApiResponse<Tour>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi tạo tour");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tour>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void updateTour(String id, Map<String, Object> body, final CallbackResult<Tour> cb) {
        api.updateAdminTour(id, body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<Tour>> call, Response<ApiResponse<Tour>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi cập nhật tour");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tour>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void deleteTour(String id, final CallbackResult<Void> cb) {
        api.deleteAdminTour(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi xóa tour");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // Upload ảnh lên Cloudinary
    public void uploadImages(List<Uri> imageUris, String tourName, final CallbackResult<List<String>> cb) {
        List<MultipartBody.Part> imageParts = new ArrayList<>();

        try {
            for (int i = 0; i < imageUris.size(); i++) {
                Uri uri = imageUris.get(i);
                File file = getFileFromUri(uri);
                if (file != null) {
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                    MultipartBody.Part part = MultipartBody.Part.createFormData("images", file.getName(), requestBody);
                    imageParts.add(part);
                }
            }

            if (imageParts.isEmpty()) {
                cb.onError("Không có ảnh hợp lệ để upload");
                return;
            }

            RequestBody tourNameBody = RequestBody.create(MediaType.parse("text/plain"), tourName);

            api.uploadImages(imageParts, tourNameBody).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        cb.onSuccess(response.body().getData());
                    } else {
                        cb.onError(response.body() != null ? response.body().getMessage() : "Lỗi upload ảnh");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                    Log.e("AdminTourRepo", "uploadImages", t);
                    cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi upload ảnh");
                }
            });
        } catch (Exception e) {
            Log.e("AdminTourRepo", "uploadImages error", e);
            cb.onError("Lỗi xử lý ảnh: " + e.getMessage());
        }
    }

    // Chuyển Uri thành File
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            Log.e("AdminTourRepo", "getFileFromUri", e);
            return null;
        }
    }

    public void searchTours(String query, final CallbackResult<List<Tour>> cb) {
        api.searchTours(query, null, null, null, null, null, null).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cb.onSuccess(response.body().getData());
                } else {
                    cb.onError(response.body() != null ? response.body().getMessage() : "Không tìm thấy tour phù hợp");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}
