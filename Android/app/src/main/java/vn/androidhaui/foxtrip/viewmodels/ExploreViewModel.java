package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.repositories.ExploreRepository;

public class ExploreViewModel extends AndroidViewModel {

    private final ExploreRepository repository;
    private final MutableLiveData<List<Tour>> toursLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repository = new ExploreRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<Tour>> getTours() { return toursLive; }
    public LiveData<Boolean> isLoading() { return loading; }

    public void applyFilters(String q,
                             java.util.List<String> provinces,
                             java.util.List<String> categories,
                             Long startDateMs,
                             Long endDateMs,
                             String priceMin,
                             String priceMax,
                             String sortOption) {

        loading.setValue(true);

        repository.searchTours(q, provinces, categories, startDateMs, endDateMs, priceMin, priceMax, sortOption,
                new ExploreRepository.OnResult() {
                    @Override
                    public void onSuccess(List<Tour> tours) {
                        loading.postValue(false);
                        toursLive.postValue(tours);
                    }

                    @Override
                    public void onError(Throwable t) {
                        loading.postValue(false);
                        toursLive.postValue(null);
                    }
                });
    }
}
