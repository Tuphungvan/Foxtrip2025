package vn.androidhaui.foxtrip.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.adapters.ChatAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentChatbotBinding;
import vn.androidhaui.foxtrip.models.ChatMessage;
import vn.androidhaui.foxtrip.viewmodels.ChatViewModel;

public class ChatbotFragment extends Fragment {

    private FragmentChatbotBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatViewModel viewModel;

    private static final String PREFS_CHAT = "chat_prefs";
    private static final String KEY_CHAT_HISTORY = "chat_history";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatbotBinding.inflate(inflater, container, false);
        setupRecycler();
        setupViewModel();
        setupListeners();
        loadLocalHistory();
        return binding.getRoot();
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(messages);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerChat.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.getBotReply().observe(getViewLifecycleOwner(), reply -> {
            if (reply != null) {
                ChatMessage botMsg = new ChatMessage(reply, false, System.currentTimeMillis());
                messages.add(botMsg);
                adapter.notifyItemInserted(messages.size() - 1);
                binding.recyclerChat.scrollToPosition(messages.size() - 1);

                saveLocalMessage(reply, false);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) addMessage("❌ " + err, false);
        });
    }

    private void setupListeners() {
        binding.btnSend.setOnClickListener(v -> sendMessage());

        binding.inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String msg = binding.inputMessage.getText() != null
                ? binding.inputMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(msg)) return;

        addMessage(msg, true);
        binding.inputMessage.setText("");

        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        viewModel.sendMessage(msg, userId);
        saveLocalMessage(msg, true);
    }

    private void addMessage(String message, boolean isUser) {
        messages.add(new ChatMessage(message, isUser, System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        binding.recyclerChat.scrollToPosition(messages.size() - 1);
    }

    private void saveLocalMessage(String message, boolean isUser) {
        long ts = System.currentTimeMillis();
        String line = ts + "|" + (isUser ? "1" : "0") + "|" + message.replace("|", " ");
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_CHAT, Context.MODE_PRIVATE);
        String prev = prefs.getString(KEY_CHAT_HISTORY, "");
        String next = prev.isEmpty() ? line : (prev + "<<<E>>>" + line);
        prefs.edit().putString(KEY_CHAT_HISTORY, next).apply();
    }

    private void loadLocalHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_CHAT, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_CHAT_HISTORY, "");
        if (raw == null || raw.isEmpty()) return;

        String[] items = raw.split("<<<E>>>");
        for (String it : items) {
            try {
                String[] parts = it.split("\\|", 3);
                long ts = Long.parseLong(parts[0]);
                boolean isUser = "1".equals(parts[1]);
                String msg = parts[2];
                messages.add(new ChatMessage(msg, isUser, ts));
            } catch (Exception ignored) {}
        }
        if (!messages.isEmpty()) adapter.notifyDataSetChanged();
        binding.recyclerChat.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
