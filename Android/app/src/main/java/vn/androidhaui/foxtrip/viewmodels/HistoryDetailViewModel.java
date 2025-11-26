package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.History;
import vn.androidhaui.foxtrip.repositories.HistoryRepository;

public class HistoryDetailViewModel extends AndroidViewModel {
    private final HistoryRepository repo;
    private final MutableLiveData<History> history = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public HistoryDetailViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new HistoryRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<History> getHistory() { return history; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadHistory(String historyId) {
        loading.setValue(true);
        repo.getHistoryDetail(historyId, new HistoryRepository.CallbackResult<>() {
            @Override
            public void onSuccess(History result) {
                loading.postValue(false);
                history.postValue(result);
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
