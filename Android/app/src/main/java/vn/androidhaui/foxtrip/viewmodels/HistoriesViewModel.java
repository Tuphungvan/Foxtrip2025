package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.History;
import vn.androidhaui.foxtrip.repositories.HistoryRepository;

public class HistoriesViewModel extends AndroidViewModel {
    private final HistoryRepository repo;
    private final MutableLiveData<List<History>> histories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public HistoriesViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new HistoryRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<History>> getHistories() { return histories; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadHistories() {
        loading.setValue(true);
        repo.getHistory(new HistoryRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<History> result) {
                loading.postValue(false);
                histories.postValue(result);
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
