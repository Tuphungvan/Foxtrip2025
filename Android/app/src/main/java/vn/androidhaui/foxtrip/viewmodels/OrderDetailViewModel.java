package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.repositories.OrderRepository;

public class OrderDetailViewModel extends AndroidViewModel {
    private final OrderRepository repo;
    private final MutableLiveData<Order> order = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }

    public void cancelOrder(String orderId) {
        loading.setValue(true);
        repo.cancelOrder(orderId, new OrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
                actionSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                actionSuccess.postValue(false);
            }
        });
    }

    public void payOrder(String orderId) {
        loading.setValue(true);
        repo.payOrder(orderId, new OrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
                actionSuccess.postValue(true);
                // Reload lại order để cập nhật trạng thái
                loadOrder(orderId);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                actionSuccess.postValue(false);
            }
        });
    }

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new OrderRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<Order> getOrder() { return order; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadOrder(String orderId) {
        loading.setValue(true);
        repo.getOrderDetail(orderId, new OrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Order result) {
                loading.postValue(false);
                order.postValue(result);
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
