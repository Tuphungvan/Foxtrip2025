package vn.androidhaui.foxtrip.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.models.CartData;
import vn.androidhaui.foxtrip.repositories.CartRepository;

public class CartViewModel extends ViewModel {
    private final CartRepository repository;

    public CartViewModel(Context context, String baseUrl) {
        repository = new CartRepository(context, baseUrl);
    }

    public LiveData<CartData> loadCart() {
        return repository.getCart();
    }

    public LiveData<String> removeFromCart(String slug) {
        return repository.removeFromCart(slug);
    }

    public LiveData<String> increase(String slug) {
        return repository.increase(slug);
    }

    public LiveData<String> decrease(String slug) {
        return repository.decrease(slug);
    }

    public LiveData<Integer> getCount() {
        return repository.getCount();
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
            if (modelClass.isAssignableFrom(CartViewModel.class)) {
                return (T) new CartViewModel(context, baseUrl);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
