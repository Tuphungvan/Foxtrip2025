package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.repositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repo;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> shouldSignOutGoogle = new MutableLiveData<>();

    private final MutableLiveData<Boolean> shouldSignOutFacebook = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AuthRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<User> getUser() { return user; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }

    public LiveData<Boolean> getShouldSignOutGoogle() { return shouldSignOutGoogle; }

    public LiveData<Boolean> getShouldSignOutFacebook() { return shouldSignOutFacebook; }

    public void resetShouldSignOutGoogle() {
        shouldSignOutGoogle.setValue(false);
    }

    public void resetShouldSignOutFacebook() {
        shouldSignOutFacebook.setValue(false);
    }

    public void login(String email, String password) {
        loading.postValue(true);
        repo.login(email, password, new AuthRepository.CallbackResult<>() {
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

    public void loginWithGoogle(String idToken) {
        loading.postValue(true);
        repo.loginWithGoogle(idToken, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                user.postValue(result);
            }
            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                if (error != null && error.contains("Tài khoản bị vô hiệu")) {
                    shouldSignOutGoogle.postValue(true);
                }
            }
        });
    }

    // Login với Facebook
    public void loginWithFacebook(String accessToken, String userID) {
        loading.postValue(true);
        repo.loginWithFacebook(accessToken, userID, new AuthRepository.CallbackResult<>() {
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

    public void register(String username, String email, String password, String phone, String address) {
        loading.postValue(true);
        repo.register(username, email, password, phone, address, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
            }
            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void logout() {
        repo.logout(new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                message.postValue(result);
                user.postValue(null);

                shouldSignOutFacebook.postValue(true);
                shouldSignOutGoogle.postValue(true);

                getApplication().getSharedPreferences("chat_prefs", 0)
                        .edit().remove("chat_history").apply();

                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(intent);
            }
            @Override
            public void onError(String error) {
                message.postValue(error);
            }
        });
    }

    public void checkLoginStatus() {
        loading.postValue(true);
        repo.checkLoginStatus(new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                user.postValue(result);
            }
            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                user.postValue(null);
            }
        });
    }

    public void clearMessage() {
        message.setValue(null);
    }
}