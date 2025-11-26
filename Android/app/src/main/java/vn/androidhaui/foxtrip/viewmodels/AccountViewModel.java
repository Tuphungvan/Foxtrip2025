package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.repositories.AccountRepository;

public class AccountViewModel extends AndroidViewModel {
    private final AccountRepository repo;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public AccountViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AccountRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<User> getUser() { return user; }
    public LiveData<String> getMessage() { return message; }

    public void loadProfile() {
        loading.postValue(true);
        repo.getProfile(new AccountRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                user.postValue(result);
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
