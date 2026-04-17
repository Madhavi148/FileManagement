package in.co.visiontek.filemanagement.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import in.co.visiontek.filemanagement.databinding.ActivityRegisterBinding;
import in.co.visiontek.filemanagement.ui.viewmodel.FileViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FileViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            String employeeId = binding.etEmployeeId.getText().toString();

            if (username.isEmpty() || password.isEmpty() || employeeId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(username, password, employeeId).observe(this, result -> {
                if ("Success".equals(result)) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.tvLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
