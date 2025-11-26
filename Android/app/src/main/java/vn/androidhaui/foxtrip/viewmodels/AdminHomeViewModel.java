package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Map;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.models.RevenueReport;
import vn.androidhaui.foxtrip.repositories.AdminHomeRepository;

public class AdminHomeViewModel extends AndroidViewModel {

    private final AdminHomeRepository repo;
    private final MutableLiveData<Map<String, Object>> overview = new MutableLiveData<>();
    private final MutableLiveData<List<RevenueReport>> revenueReports = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public AdminHomeViewModel(@NonNull Application app) {
        super(app);
        String baseUrl = app.getApplicationContext().getString(R.string.base_url);
        repo = new AdminHomeRepository(app.getApplicationContext(), baseUrl);
    }

    public LiveData<Map<String, Object>> getOverview() { return overview; }
    public LiveData<List<RevenueReport>> getRevenueReports() { return revenueReports; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void loadOverview() {
        loading.postValue(true);
        repo.getOverview(new AdminHomeRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                loading.postValue(false);
                overview.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void loadRevenue() {
        loading.postValue(true);
        repo.getRevenue(new AdminHomeRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<RevenueReport> result) {
                loading.postValue(false);
                revenueReports.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void clearMessage() {
        message.setValue(null);
    }
}
