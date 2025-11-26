package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.repositories.OrderRepository;

public class OrdersViewModel extends AndroidViewModel {
    private final OrderRepository repo;
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new OrderRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<Order>> getOrders() { return orders; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadOrders() {
        loading.setValue(true);
        repo.getMyOrders(new OrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(List<Order> result) {
                loading.postValue(false);
                orders.postValue(result);
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
