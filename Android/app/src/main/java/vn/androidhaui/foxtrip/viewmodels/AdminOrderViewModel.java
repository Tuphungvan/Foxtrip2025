package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.models.AdminOrderResponse;
import vn.androidhaui.foxtrip.models.SearchOrderResponse;
import vn.androidhaui.foxtrip.repositories.AdminOrderRepository;

public class AdminOrderViewModel extends AndroidViewModel {
    private final AdminOrderRepository repo;
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();

    private final MutableLiveData<SearchOrderResponse> searchResult = new MutableLiveData<>();

    public AdminOrderViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AdminOrderRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<List<Order>> getOrders() { return orders; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getActionResult() { return actionResult; }

    public LiveData<SearchOrderResponse> getSearchResult() { return searchResult; }


    public void loadOrders(String type) {
        loading.postValue(true);
        repo.fetchOrdersByType(type, new AdminOrderRepository.CallbackResult<>() {
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

    // tìm order

    public void searchOrderById(String orderId) {
        loading.postValue(true);
        repo.searchOrderById(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(SearchOrderResponse result) {
                loading.postValue(false);
                searchResult.postValue(result); // Post vào LiveData riêng
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void deletePendingOrder(String orderId) {
        loading.postValue(true);
        repo.deletePendingOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                actionResult.postValue("delete_success:" + result.getOrderId());
                loadOrders("pending");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void confirmOrder(String orderId) {
        loading.postValue(true);
        repo.confirmOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                actionResult.postValue("confirm_success:" + result.getOrderId());
                loadOrders("to_confirm");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void completeExpiredOrder(String orderId) {
        loading.postValue(true);
        repo.completeExpiredOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                actionResult.postValue("complete_success:" + result.getOrderId());
                loadOrders("completed");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }
}
