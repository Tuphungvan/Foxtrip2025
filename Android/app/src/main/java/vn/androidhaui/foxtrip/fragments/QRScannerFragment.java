package vn.androidhaui.foxtrip.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.List;

import vn.androidhaui.foxtrip.databinding.FragmentQrScannerBinding;

public class QRScannerFragment extends Fragment {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private FragmentQrScannerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Kiểm tra quyền camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            startScanning();
        }

        binding.btnClose.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void startScanning() {
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    String orderId = result.getText();

                    // Kiểm tra format ObjectId (24 ký tự hex)
                    if (orderId.matches("^[0-9a-fA-F]{24}$")) {
                        binding.barcodeScanner.pause();

                        // Mở AdminOrderDetailFragment
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(android.R.id.content, AdminOrderDetailFragment.newInstance(orderId))
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(requireContext(),
                                "QR code không hợp lệ",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Không cần xử lý
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(requireContext(), "Cần cấp quyền camera để quét QR",
                        Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.barcodeScanner != null) {
            binding.barcodeScanner.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null && binding.barcodeScanner != null) {
            binding.barcodeScanner.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}