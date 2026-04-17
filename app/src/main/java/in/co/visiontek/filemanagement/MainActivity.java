package in.co.visiontek.filemanagement;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import in.co.visiontek.filemanagement.data.model.FileModel;
import in.co.visiontek.filemanagement.data.model.UserWithCount;
import in.co.visiontek.filemanagement.databinding.ActivityMainBinding;
import in.co.visiontek.filemanagement.ui.LoginActivity;
import in.co.visiontek.filemanagement.ui.adapter.FileAdapter;
import in.co.visiontek.filemanagement.ui.adapter.UserAdapter;
import in.co.visiontek.filemanagement.ui.viewmodel.FileViewModel;
import in.co.visiontek.filemanagement.util.SessionManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FileViewModel viewModel;
    private FileAdapter fileAdapter;
    private UserAdapter userAdapter;
    private ActivityResultLauncher<String> filePickerLauncher;
    private SessionManager sessionManager;
    private String selectedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(FileViewModel.class);

        setupRecyclerViews();
        setupFileUpload();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (sessionManager.isAdmin() && selectedUser != null) {
                    selectedUser = null;
                    loadData();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupRecyclerViews() {
        fileAdapter = new FileAdapter(new ArrayList<>(), new FileAdapter.OnFileClickListener() {
            @Override
            public void onDownloadClick(FileModel file) {
                downloadFile(file);
            }

            @Override
            public void onQRClick(FileModel file) {
                showQRDialog(file);
            }

            @Override
            public void onDeleteClick(FileModel file) {
                confirmDeleteFile(file);
            }
        });

        userAdapter = new UserAdapter(new ArrayList<>(), new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(String username) {
                selectedUser = username;
                loadData();
            }

            @Override
            public void onDeleteFolderClick(String username) {
                confirmDeleteFolder(username);
            }
        });

        binding.rvFiles.setLayoutManager(new LinearLayoutManager(this));
    }

    private void confirmDeleteFile(FileModel file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete " + file.getFileName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteFile(file.getId()).observe(this, result -> {
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                        if ("Success".equals(result)) {
                            loadData();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteFolder(String username) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Folder")
                .setMessage("Are you sure you want to delete all files for user: " + username + "? This cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    viewModel.deleteUserFolder(username).observe(this, result -> {
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                        if ("Success".equals(result)) {
                            loadData();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);
        if (sessionManager.isAdmin()) {
            if (selectedUser == null) {
                binding.toolbar.setTitle("Users Folders");
                binding.fabUpload.setVisibility(View.GONE);
                binding.rvFiles.setAdapter(userAdapter);
                viewModel.getUploadedUsers().observe(this, users -> {
                    binding.swipeRefresh.setRefreshing(false);
                    if (users != null) {
                        userAdapter.setUsers(users);
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.toolbar.setTitle(selectedUser + "'s Files");
                binding.fabUpload.setVisibility(View.GONE);
                binding.rvFiles.setAdapter(fileAdapter);
                viewModel.getFiles(selectedUser, true).observe(this, files -> {
                    binding.swipeRefresh.setRefreshing(false);
                    if (files != null) {
                        fileAdapter.setFiles(files);
                    } else {
                        Toast.makeText(this, "Failed to load files", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            binding.toolbar.setTitle("My Files");
            binding.fabUpload.setVisibility(View.VISIBLE);
            binding.rvFiles.setAdapter(fileAdapter);
            viewModel.getFiles(sessionManager.getUsername(), false).observe(this, files -> {
                binding.swipeRefresh.setRefreshing(false);
                if (files != null) {
                    fileAdapter.setFiles(files);
                } else {
                    Toast.makeText(this, "Failed to load files", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showQRDialog(FileModel fileModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        try {
            Bitmap bitmap = generateQRCode(fileModel.getUrl());
            imageView.setImageBitmap(bitmap);
            builder.setView(imageView);
            builder.setTitle(fileModel.getFileName());
            builder.setPositiveButton("Download", (dialog, which) -> downloadFile(fileModel));
            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }

    private void setupFileUpload() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadFile(uri);
            }
        });

        binding.fabUpload.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
    }

    private void uploadFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), fileName);
            copyInputStreamToFile(inputStream, file);

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);
            RequestBody username = RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUsername());

            viewModel.uploadFile(body, username).observe(this, result -> {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                loadData();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(FileModel fileModel) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileModel.getUrl()));
        request.setTitle(fileModel.getFileName());
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileModel.getFileName());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void copyInputStreamToFile(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
