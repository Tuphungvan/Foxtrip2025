package vn.androidhaui.foxtrip;

import com.facebook.appevents.AppEventsLogger;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.androidhaui.foxtrip.databinding.ActivityMainBinding;
import vn.androidhaui.foxtrip.fragments.AccountFragment;
import vn.androidhaui.foxtrip.fragments.CartFragment;
import vn.androidhaui.foxtrip.fragments.ChatbotFragment;
import vn.androidhaui.foxtrip.fragments.ExploreFragment;
import vn.androidhaui.foxtrip.fragments.HomeFragment;
import vn.androidhaui.foxtrip.fragments.LoginFragment;
import vn.androidhaui.foxtrip.fragments.VideoFragment;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.viewmodels.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Status bar màu
        Window window = getWindow();

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //full screen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Status bar trong suốt
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Khởi tạo AuthViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Quan sát user LiveData
        authViewModel.getUser().observe(this, this::handleUserSession);

        // Quan sát thông báo (Toast)
        authViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                authViewModel.clearMessage(); // tránh hiển thị lại khi xoay màn hình
            }
        });

        // Quan sát trạng thái loading nếu cần (progress bar)
        authViewModel.getLoading().observe(this, isLoading -> {
            // Hiển thị hoặc ẩn ProgressBar nếu có
        });

        // Kiểm tra trạng thái đăng nhập ban đầu
        if (savedInstanceState == null) {
            authViewModel.checkLoginStatus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplication());
    }

    private void handleUserSession(User currentUser) {
        if (currentUser != null) {
            // Nếu là admin thì chuyển sang AdminActivity
            if (currentUser.isAdmin()) {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            // Người dùng bình thường
            loadMainFragments();
            binding.getRoot().post(() -> loadFragment(new HomeFragment(), true));
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            // Người dùng chưa đăng nhập
            binding.bottomNavigation.setVisibility(BottomNavigationView.GONE);
            binding.getRoot().post(() -> loadFragment(new LoginFragment(), false));
        }
    }

    private void loadMainFragments() {
        binding.bottomNavigation.setVisibility(BottomNavigationView.VISIBLE);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) selected = new HomeFragment();
            else if (id == R.id.nav_account) selected = new AccountFragment();
            else if (id == R.id.nav_cart) selected = new CartFragment();
            else if (id == R.id.nav_chatbot) selected = new ChatbotFragment();
            else if (id == R.id.nav_explore) selected = new ExploreFragment();
            else if (id == R.id.nav_video) selected = new VideoFragment();

            if (selected != null) {
                loadFragment(selected, true);
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment, boolean showBottomNav) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // đây là phần dùng tham số showBottomNav
        binding.bottomNavigation.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
    }

    public void updateCartBadge(int count) {
        if (binding == null) return;

        binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart).setVisible(count > 0);
        binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart).setNumber(count);
    }

    // Được gọi từ LoginFragment khi login thành công
    public void onLoginSuccess() {
        // LiveData observer sẽ tự xử lý
    }
}
