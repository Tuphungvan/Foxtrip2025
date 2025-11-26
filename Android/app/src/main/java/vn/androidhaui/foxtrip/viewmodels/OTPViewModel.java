package vn.androidhaui.foxtrip.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.repositories.OTPRepository;

public class OTPViewModel extends ViewModel {
    private final OTPRepository repo;

    public OTPViewModel(Context context, String baseUrl) {
        repo = new OTPRepository(context, baseUrl);
    }

    public LiveData<String> getMessage() {
        return repo.getMessage();
    }

    public LiveData<Boolean> sendOTP(String email, String username) {
        return repo.sendOTP(email, username);
    }

    public LiveData<Boolean> verifyOTP(String email, String code) {
        return repo.verifyOTP(email, code);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context ctx;
        private final String baseUrl;

        public Factory(Context ctx, String baseUrl) {
            this.ctx = ctx;
            this.baseUrl = baseUrl;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(OTPViewModel.class)) {
                return (T) new OTPViewModel(ctx, baseUrl);
            }
            throw new IllegalArgumentException("Unknown ViewModel");
        }
    }
}