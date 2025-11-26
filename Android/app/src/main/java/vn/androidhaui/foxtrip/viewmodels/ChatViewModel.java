package vn.androidhaui.foxtrip.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.repositories.ChatRepository;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<String> botReply = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        // Lấy baseUrl từ strings.xml
        String baseUrl = application.getString(R.string.base_url);
        repository = new ChatRepository(application.getApplicationContext(), baseUrl);
    }

    public LiveData<String> getBotReply() { return botReply; }
    public LiveData<String> getError() { return error; }

    public void sendMessage(String message, String userId) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        if (userId != null) body.put("userId", userId);

        repository.sendMessage(body, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                botReply.postValue(reply);
            }

            @Override
            public void onError(String err) {
                error.postValue(err);
            }
        });
    }
}
