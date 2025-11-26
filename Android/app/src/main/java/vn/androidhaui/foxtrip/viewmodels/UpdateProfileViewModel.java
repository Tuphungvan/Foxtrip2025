package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.repositories.UpdateProfileRepository;

public class UpdateProfileViewModel extends AndroidViewModel {

    private final UpdateProfileRepository repo;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // 🟡 Thêm biến này để báo cho Fragment biết khi nào update thành công
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public UpdateProfileViewModel(@NonNull Application app) {
        super(app);
        repo = new UpdateProfileRepository(
                app.getApplicationContext(),
                app.getString(vn.androidhaui.foxtrip.R.string.base_url)
        );
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void loadProfile() {
        repo.getUpdateProfile(new UpdateProfileRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                user.postValue(result);
            }

            @Override
            public void onError(String error) {
                message.postValue(error);
            }
        });
    }

    public void updateProfile(Map<String, Object> body) {
        repo.postUpdateProfile(body, new UpdateProfileRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                message.postValue("Cập nhật thành công!");
                updateSuccess.postValue(true); // 🟡 báo thành công
            }

            @Override
            public void onError(String error) {
                message.postValue(error);
                updateSuccess.postValue(false); // 🟡 báo thất bại
            }
        });
    }

    public void clearMessage() {
        message.setValue(null);
    }
}
