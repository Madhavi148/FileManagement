package in.co.visiontek.filemanagement.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import in.co.visiontek.filemanagement.data.api.ApiService;
import in.co.visiontek.filemanagement.data.model.FileModel;
import in.co.visiontek.filemanagement.data.model.User;
import in.co.visiontek.filemanagement.data.model.UserWithCount;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FileRepository {
    private static final String TAG = "FileRepository";
    private final ApiService apiService;
    
    // REPLACE THIS with your actual Render URL (e.g., https://your-app.onrender.com)
    private static final String BASE_URL = "https://your-file-management-app.onrender.com"; 

    public FileRepository() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    // ... (rest of the code remains the same)
    public LiveData<String> login(String username, String password) {
        MutableLiveData<String> loginResult = new MutableLiveData<>();
        User user = new User(username, password, null);
        apiService.login(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Login Success");
                    loginResult.postValue("Success");
                } else {
                    Log.e(TAG, "Login Failed: " + response.code() + " " + response.message());
                    loginResult.postValue("Login Failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Login Error: " + t.getMessage(), t);
                loginResult.postValue("Error: " + t.getMessage());
            }
        });
        return loginResult;
    }

    public LiveData<String> register(String username, String password, String employeeId) {
        MutableLiveData<String> registerResult = new MutableLiveData<>();
        User user = new User(username, password, employeeId);
        Log.d(TAG, "Registering user: " + username + " with URL: " + BASE_URL);
        apiService.register(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Registration Success");
                    registerResult.postValue("Success");
                } else {
                    Log.e(TAG, "Registration Failed: " + response.code() + " " + response.message());
                    registerResult.postValue("Registration Failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Registration Error: " + t.getMessage(), t);
                registerResult.postValue("Error: " + t.getMessage());
            }
        });
        return registerResult;
    }

    public LiveData<List<FileModel>> getFiles(String username, boolean isAdmin) {
        MutableLiveData<List<FileModel>> filesData = new MutableLiveData<>();
        apiService.getFiles(username, isAdmin).enqueue(new Callback<List<FileModel>>() {
            @Override
            public void onResponse(Call<List<FileModel>> call, Response<List<FileModel>> response) {
                if (response.isSuccessful()) {
                    filesData.postValue(response.body());
                } else {
                    Log.e(TAG, "GetFiles Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FileModel>> call, Throwable t) {
                Log.e(TAG, "GetFiles Error: " + t.getMessage(), t);
                filesData.postValue(null);
            }
        });
        return filesData;
    }

    public LiveData<List<UserWithCount>> getUploadedUsers() {
        MutableLiveData<List<UserWithCount>> usersData = new MutableLiveData<>();
        apiService.getUploadedUsers().enqueue(new Callback<List<UserWithCount>>() {
            @Override
            public void onResponse(Call<List<UserWithCount>> call, Response<List<UserWithCount>> response) {
                if (response.isSuccessful()) {
                    usersData.postValue(response.body());
                } else {
                    Log.e(TAG, "getUploadedUsers Failed: " + response.code());
                    usersData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<UserWithCount>> call, Throwable t) {
                Log.e(TAG, "getUploadedUsers Error: " + t.getMessage(), t);
                usersData.postValue(null);
            }
        });
        return usersData;
    }

    public LiveData<String> deleteFile(String fileId) {
        MutableLiveData<String> deleteResult = new MutableLiveData<>();
        apiService.deleteFile(fileId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    deleteResult.postValue("Success");
                } else {
                    deleteResult.postValue("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteResult.postValue("Error: " + t.getMessage());
            }
        });
        return deleteResult;
    }

    public LiveData<String> deleteUserFolder(String username) {
        MutableLiveData<String> deleteResult = new MutableLiveData<>();
        apiService.deleteUserFolder(username).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    deleteResult.postValue("Success");
                } else {
                    deleteResult.postValue("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteResult.postValue("Error: " + t.getMessage());
            }
        });
        return deleteResult;
    }

    public LiveData<String> uploadFile(MultipartBody.Part file, RequestBody username) {
        MutableLiveData<String> uploadResult = new MutableLiveData<>();
        apiService.uploadFile(file, username).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    uploadResult.postValue("Upload Success");
                } else {
                    Log.e(TAG, "Upload Failed: " + response.code());
                    uploadResult.postValue("Upload Failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Upload Error: " + t.getMessage(), t);
                uploadResult.postValue("Error: " + t.getMessage());
            }
        });
        return uploadResult;
    }
}
