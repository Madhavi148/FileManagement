package in.co.visiontek.filemanagement.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import in.co.visiontek.filemanagement.MainActivity;
import in.co.visiontek.filemanagement.databinding.ActivityLoginBinding;
import in.co.visiontek.filemanagement.ui.viewmodel.FileViewModel;
import in.co.visiontek.filemanagement.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FileViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FileViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(username, password).observe(this, result -> {
                if ("Success".equals(result)) {
                    boolean isAdmin = "admin".equalsIgnoreCase(username);
                    sessionManager.createLoginSession(username, isAdmin);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
