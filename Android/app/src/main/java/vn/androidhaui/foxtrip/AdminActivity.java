package vn.androidhaui.foxtrip;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import vn.androidhaui.foxtrip.databinding.ActivityAdminBinding;
import vn.androidhaui.foxtrip.fragments.AdminHomeFragment;
import vn.androidhaui.foxtrip.fragments.AdminOrdersFragment;
import vn.androidhaui.foxtrip.fragments.AdminToursFragment;
import vn.androidhaui.foxtrip.fragments.AdminUsersFragment;
import vn.androidhaui.foxtrip.fragments.CreateAdminFragment;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đặt màu status bar
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fullscreen layout
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Status bar trong suốt
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Nếu lần đầu mở, load trang chủ admin
        if (savedInstanceState == null) {
            binding.getRoot().post(() -> loadFragment(new AdminHomeFragment(), true));
            binding.adminBottomNav.setSelectedItemId(R.id.nav_admin_home);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.adminBottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_admin_home) {
                selected = new AdminHomeFragment();
            }
            else if (id == R.id.nav_create_admin) {
                selected = new CreateAdminFragment();
            }
            else if (id == R.id.nav_users_manager) {
                selected = new AdminUsersFragment();
            }
            else if (id == R.id.nav_tours_manager) {
                selected = new AdminToursFragment();
            }
            else if (id == R.id.nav_orders_manager) {
                selected = new AdminOrdersFragment();
            }

            if (selected != null) {
                loadFragment(selected, true);
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment, boolean showBottomNav) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        binding.adminBottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
    }
}
