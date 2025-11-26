package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import vn.androidhaui.foxtrip.repositories.WalletRepository;

public class WalletViewModel extends AndroidViewModel {

    private final WalletRepository repo;
    private final MutableLiveData<Double> balance = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public WalletViewModel(@NonNull Application application) {
        super(application);
        repo = new WalletRepository(application.getApplicationContext());
    }

    public LiveData<Double> getBalance() {
        return balance;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void clearMessage() {
        message.setValue(null);
    }

    public void loadWallet() {
        repo.getWallet(new WalletRepository.WalletCallback<>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (data != null && data.containsKey("balance")) {
                    Object value = data.get("balance");
                    if (value instanceof Number) {
                        balance.setValue(((Number) value).doubleValue());
                    } else {
                        balance.setValue(Double.parseDouble(value.toString()));
                    }
                }
            }

            @Override
            public void onError(String error) {
                message.setValue(error);
            }
        });
    }

    public void recharge(String amount) {
        repo.recharge(amount, new WalletRepository.WalletCallback<>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                message.setValue("Nạp tiền thành công!");
                loadWallet(); // cập nhật lại số dư
            }

            @Override
            public void onError(String error) {
                message.setValue(error);
            }
        });
    }
}
