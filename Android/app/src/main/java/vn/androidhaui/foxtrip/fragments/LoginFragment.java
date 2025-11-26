package vn.androidhaui.foxtrip.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

//facebook
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.facebook.login.LoginBehavior;

import vn.androidhaui.foxtrip.AdminActivity;
import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentLoginBinding;
import vn.androidhaui.foxtrip.viewmodels.AuthViewModel;

import java.util.Arrays;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private CallbackManager facebookCallbackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupGoogleSignIn();
        setupFacebookLogin();

        return binding.getRoot();
    }

    private void setupGoogleSignIn() {
        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Launcher để nhận kết quả từ Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
        );
    }

    // THÊM PHƯƠNG THỨC THIẾT LẬP FACEBOOK
    private void setupFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        String token = accessToken.getToken();
                        String userId = accessToken.getUserId();
                        Log.d(TAG, "Facebook login success, sending to backend");
                        viewModel.loginWithFacebook(token, userId);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(requireContext(), "Đăng nhập Facebook bị hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull FacebookException error) {
                        Toast.makeText(requireContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Facebook login error", error);
                    }
                });
    }

    private void promptGoogleAccountSelection() {
        // Sign out khỏi Google trước khi sign in để buộc chọn tài khoản
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            // Sau khi sign out, bắt đầu sign in
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void signOutGoogle() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut()
                    .addOnCompleteListener(requireActivity(), task -> {
                        Log.d(TAG, "Google Sign-Out cục bộ thành công.");
                        // Reset cờ báo hiệu để tránh gọi lại nhiều lần
                        viewModel.resetShouldSignOutGoogle();
                    });
        }
    }

    private void signOutFacebook() {
        LoginManager.getInstance().logOut();
        Log.d(TAG, "Facebook Sign-Out cục bộ thành công.");
        viewModel.resetShouldSignOutFacebook();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewModel.getShouldSignOutFacebook().observe(getViewLifecycleOwner(), shouldSignOut -> {
            if (shouldSignOut != null && shouldSignOut) {
                signOutFacebook();
            }
        });

        viewModel.getShouldSignOutGoogle().observe(getViewLifecycleOwner(), shouldSignOut -> {
            if (shouldSignOut != null && shouldSignOut) {
                signOutGoogle();
            }
        });

        // Observe user
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (user.isAdmin()) {
                Intent intent = new Intent(requireActivity(), AdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            } else {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onLoginSuccess();
                }
            }
        });

        // Observe message
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Login thông thường
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        // ✅ Login với Google
        binding.btnGoogleLogin.setOnClickListener(v ->
            promptGoogleAccountSelection()
        );

        binding.btnFacebookLogin.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();

            LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);

            LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    Arrays.asList("public_profile")
            );
        });

        // Go to Register
        binding.tvGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterFragment(), false);
            }
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                Log.d(TAG, "Google Sign-In success, sending idToken to backend");
                viewModel.loginWithGoogle(idToken);
            } else {
                Toast.makeText(requireContext(), "Không thể lấy Google ID Token", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed: " + e.getStatusCode(), e);
            Toast.makeText(requireContext(), "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}