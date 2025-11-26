package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.repositories.AdminTourRepository;

public class AdminTourViewModel extends AndroidViewModel {
    private final AdminTourRepository repo;
    private final MutableLiveData<List<Tour>> tours = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Tour> currentTour = new MutableLiveData<>();

    // Quản lý ảnh
    private final MutableLiveData<List<String>> imageUrls = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> deletedImageUrls = new MutableLiveData<>(new ArrayList<>());

    public AdminTourViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AdminTourRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<Tour>> getTours() { return tours; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Tour> getCurrentTour() { return currentTour; }

    public LiveData<List<String>> getImageUrls() { return imageUrls; }
    public LiveData<List<String>> getDeletedImageUrls() { return deletedImageUrls; }

    public void loadTours() {
        loading.postValue(true);
        repo.fetchTours(new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<Tour> result) {
                loading.postValue(false);
                tours.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void setCurrentTour(Tour t) {
        currentTour.postValue(t);
        if (t != null && t.getImage() != null) {
            imageUrls.postValue(new ArrayList<>(t.getImage()));
        } else {
            imageUrls.postValue(new ArrayList<>());
        }
        deletedImageUrls.postValue(new ArrayList<>());
    }

    public void clearCurrentTour() {
        currentTour.postValue(null);
        imageUrls.postValue(new ArrayList<>());
        deletedImageUrls.postValue(new ArrayList<>());
    }

    public void clearMessage() {
        message.postValue(null);
    }

    public void addImages(List<Uri> newImageUris, String tourName) {
        loading.postValue(true);
        repo.uploadImages(newImageUris, tourName, new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<String> uploadedUrls) {
                loading.postValue(false);
                List<String> current = imageUrls.getValue();
                if (current != null) {
                    current.addAll(uploadedUrls);
                    imageUrls.postValue(current);
                    message.postValue("Thêm " + uploadedUrls.size() + " ảnh thành công");
                }
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    // Xóa ảnh
    public void removeImage(String imageUrl) {
        List<String> current = imageUrls.getValue();
        if (current != null) {
            current.remove(imageUrl);
            imageUrls.postValue(current);

            // Thêm vào danh sách ảnh đã xóa
            List<String> deleted = deletedImageUrls.getValue();
            if (deleted != null) {
                deleted.add(imageUrl);
                deletedImageUrls.postValue(deleted);
            }
        }
    }

    public void createTour(Map<String, Object> body) {
        // Thêm danh sách ảnh vào body
        body.put("images", imageUrls.getValue());

        loading.postValue(true);
        repo.createTour(body, new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Tour result) {
                loading.postValue(false);
                loadTours();
                message.postValue("Tạo tour thành công");
                clearCurrentTour();
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void updateTour(String id, Map<String, Object> body) {
        // Thêm danh sách ảnh và ảnh đã xóa
        body.put("images", imageUrls.getValue());
        body.put("deletedImages", deletedImageUrls.getValue());

        loading.postValue(true);
        repo.updateTour(id, body, new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Tour result) {
                loading.postValue(false);
                loadTours();
                message.postValue("Cập nhật tour thành công");
                clearCurrentTour();
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void deleteTour(String id) {
        loading.postValue(true);
        repo.deleteTour(id, new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                loadTours();
                message.postValue("Xóa tour thành công");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void searchTours(String query) {
        loading.postValue(true);
        repo.searchTours(query, new AdminTourRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<Tour> result) {
                loading.postValue(false);
                tours.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }
}
