package vn.androidhaui.foxtrip.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.models.CheckoutData;
import vn.androidhaui.foxtrip.models.OrderResponse;
import vn.androidhaui.foxtrip.models.PaymentData;
import vn.androidhaui.foxtrip.repositories.CheckoutRepository;

public class CheckoutViewModel extends ViewModel {
    private final CheckoutRepository repo;

    public CheckoutViewModel(Context context, String baseUrl) {
        repo = new CheckoutRepository(context, baseUrl);
    }

    public LiveData<CheckoutData> getCheckout() { return repo.getCheckout(); }

    public LiveData<String> getMessage() {
        return repo.getMessage();
    }

    public LiveData<OrderResponse> placeOrder(String username, String phoneNumber, String address, String email) {
        return repo.placeOrder(username, phoneNumber, address, email);
    }

    public LiveData<PaymentData> getPaymentInfo(String orderId) { return repo.getPaymentInfo(orderId); }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context ctx;
        private final String baseUrl;

        public Factory(Context ctx, String baseUrl) {
            this.ctx = ctx;
            this.baseUrl = baseUrl;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CheckoutViewModel.class)) {
                return (T) new CheckoutViewModel(ctx, baseUrl);
            }
            throw new IllegalArgumentException("Unknown ViewModel");
        }
    }
}
