package vn.androidhaui.foxtrip.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentAccountBinding;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.repositories.AccountRepository;
import vn.androidhaui.foxtrip.viewmodels.AccountViewModel;
import vn.androidhaui.foxtrip.viewmodels.AuthViewModel;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::renderUser);
        profileViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                profileViewModel.clearMessage();
            }
        });

        // load profile khi mở fragment
        profileViewModel.loadProfile();

        // option placeholders
        binding.optionOrders.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit()
        );

        binding.optionHistory.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HistoriesFragment())
                    .addToBackStack(null)
                    .commit()
        );

        binding.optionWallet.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RechargeWalletFragment())
                    .addToBackStack(null)
                    .commit()
        );

        binding.optionUpdate.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UpdateProfileFragment())
                    .addToBackStack(null)
                    .commit()
        );

        binding.optionAbout.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AboutFoxtripFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // logout
        binding.btnLogout.setOnClickListener(v ->
            authViewModel.logout()
        );

        // khởi tạo image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Glide.with(this)
                                .load(uri)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.imgAvatar);
                        uploadAvatar(uri);
                    }
                }
        );

        // click vào avatar để chọn ảnh
        binding.imgAvatar.setOnClickListener(v ->
            imagePickerLauncher.launch("image/*")
        );
    }

    private void renderUser(User user) {
        if (user == null) {
            binding.tvUsername.setText("Khách");
            binding.tvEmail.setText("");
            binding.imgAvatar.setImageResource(R.drawable.ic_account);
            return;
        }
        binding.tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        String avatar = user.getAvatar();
        if (avatar == null || avatar.isEmpty()) {
            binding.imgAvatar.setImageResource(R.drawable.ic_account);
        } else {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.ic_account)
                    .into(binding.imgAvatar);
        }
    }

    private void uploadAvatar(Uri uri) {
        try {
            File file = createFileFromUri(uri);

            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), reqFile);

            AccountRepository repo = new AccountRepository(requireContext(), getString(R.string.base_url));
            repo.uploadAvatar(body, new AccountRepository.CallbackResult<>() {
                @Override
                public void onSuccess(String url) {
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                    profileViewModel.loadProfile(); // refresh lại thông tin
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Lỗi upload: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private File createFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = new File(requireContext().getCacheDir(), "temp_avatar.jpg");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return tempFile;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
