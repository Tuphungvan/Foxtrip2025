package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.repositories.AdminUserRepository;

public class AdminUserViewModel extends AndroidViewModel {
    private final AdminUserRepository repo;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public AdminUserViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AdminUserRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void loadUsers(String search, String sort) {
        loading.postValue(true);
        repo.fetchUsers(search, sort, new AdminUserRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<User> result) {
                loading.postValue(false);
                users.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void deactivateUser(String id) {
        loading.postValue(true);
        repo.deactivateUser(id, new AdminUserRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                loadUsers(null, null);
                message.postValue("Khoá người dùng thành công");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void activateUser(String id) {
        loading.postValue(true);
        repo.activateUser(id, new AdminUserRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                loadUsers(null, null);
                message.postValue("Kích hoạt người dùng thành công");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void resetPassword(String id) {
        loading.postValue(true);
        repo.resetPassword(id, new AdminUserRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                message.postValue("Reset mật khẩu thành công");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void clearMessage() { message.setValue(null); }
}
