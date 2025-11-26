package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.AdminOrderResponse;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.repositories.AdminOrderRepository;

public class AdminOrderDetailViewModel extends AndroidViewModel {
    private final AdminOrderRepository repo;
    private final MutableLiveData<Order> order = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);

    public AdminOrderDetailViewModel(@NonNull Application application) {
        super(application);
        String baseUrl = application.getApplicationContext().getString(vn.androidhaui.foxtrip.R.string.base_url);
        repo = new AdminOrderRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<Order> getOrder() { return order; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }

    public void clearMessage() {
        message.postValue(null);
    }

    // Load chi tiết order
    public void loadOrder(String orderId) {
        loading.postValue(true);
        repo.fetchOrderDetail(orderId, new AdminOrderRepository.CallbackResult<>() {
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

    // Xóa đơn hàng (chờ thanh toán)
    public void deleteOrder(String orderId) {
        loading.postValue(true);
        repo.deletePendingOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                message.postValue("Đã xóa đơn hàng");
                actionSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    // Xác nhận đơn hàng (đã thanh toán -> hoàn tất)
    public void confirmOrder(String orderId) {
        loading.postValue(true);
        repo.confirmOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                message.postValue("Đã xác nhận đơn hàng");
                actionSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    // Hoàn tất đơn hàng (chuyển vào lịch sử)
    public void completeOrder(String orderId) {
        loading.postValue(true);
        repo.completeExpiredOrder(orderId, new AdminOrderRepository.CallbackResult<>() {
            @Override
            public void onSuccess(AdminOrderResponse result) {
                loading.postValue(false);
                message.postValue("Đã hoàn tất đơn hàng");
                actionSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }
}