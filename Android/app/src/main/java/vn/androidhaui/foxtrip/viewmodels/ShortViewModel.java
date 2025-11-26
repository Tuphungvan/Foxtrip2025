package vn.androidhaui.foxtrip.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.repositories.ShortRepository;

public class ShortViewModel extends ViewModel {
    private final ShortRepository repository;

    public ShortViewModel(Context context, String baseUrl) {
        repository = new ShortRepository(context, baseUrl);
    }

    /**
     * Gọi API lấy video ngẫu nhiên
     */
    public LiveData<Tour> getRandomShort() {
        return repository.getRandomShort();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context context;
        private final String baseUrl;

        public Factory(Context context, String baseUrl) {
            this.context = context;
            this.baseUrl = baseUrl;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ShortViewModel.class)) {
                return (T) new ShortViewModel(context, baseUrl);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}