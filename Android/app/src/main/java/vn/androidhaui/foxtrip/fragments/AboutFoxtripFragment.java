package vn.androidhaui.foxtrip.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.androidhaui.foxtrip.databinding.FragmentAboutFoxtripBinding;

public class AboutFoxtripFragment extends Fragment {

    private FragmentAboutFoxtripBinding binding;
    private static final String PHONE_NUMBER = "0859605024";
    private static final String EMAIL = "cloneappadobe@gmail.com";
    private static final String FANPAGE_URL = "https://www.facebook.com/foxtrip.2025/";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutFoxtripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý nút back trên toolbar
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Click vào Hotline để gọi điện
        binding.layoutPhone.setOnClickListener(v -> makePhoneCall());

        // Click vào Email để gửi email
        binding.layoutEmail.setOnClickListener(v -> sendEmail());

        // Click vào Fanpage để mở Facebook
        binding.layoutFanpage.setOnClickListener(v -> openFanpage());

        // Click vào Zalo để mở Zalo
        binding.layoutZalo.setOnClickListener(v -> openZalo());
    }

    private void makePhoneCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ từ FoxTrip App");
            startActivity(Intent.createChooser(intent, "Gửi email qua"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể gửi email", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFanpage() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(FANPAGE_URL));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể mở Fanpage", Toast.LENGTH_SHORT).show();
        }
    }

    private void openZalo() {
        try {
            // Thử mở Zalo app trực tiếp
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://zalo.me/" + PHONE_NUMBER));
            startActivity(intent);
        } catch (Exception e) {
            // Nếu không mở được, fallback sang web browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://chat.zalo.me/?phone=" + PHONE_NUMBER));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "Không thể mở Zalo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}