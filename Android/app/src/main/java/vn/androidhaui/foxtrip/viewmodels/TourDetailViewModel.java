package vn.androidhaui.foxtrip.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.repositories.TourRepository;

public class TourDetailViewModel extends ViewModel {
    private final TourRepository repository;

    public TourDetailViewModel(Context context, String baseUrl) {
        repository = new TourRepository(context, baseUrl);
    }

    public LiveData<Tour> loadTourDetail(String slug) {
        return repository.getTourDetail(slug);
    }

    public LiveData<String> addToCart(String slug, int quantity) {
        return repository.addToCart(slug, quantity);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context context;
        private final String baseUrl;

        public Factory(Context context, String baseUrl) {
            this.context = context;
            this.baseUrl = baseUrl;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TourDetailViewModel.class)) {
                return (T) new TourDetailViewModel(context, baseUrl);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
