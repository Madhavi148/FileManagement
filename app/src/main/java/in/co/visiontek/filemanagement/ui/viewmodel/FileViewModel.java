package in.co.visiontek.filemanagement.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import in.co.visiontek.filemanagement.data.model.FileModel;
import in.co.visiontek.filemanagement.data.model.UserWithCount;
import in.co.visiontek.filemanagement.data.repository.FileRepository;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileViewModel extends ViewModel {
    private final FileRepository repository;

    public FileViewModel() {
        this.repository = new FileRepository();
    }

    public LiveData<String> login(String username, String password) {
        return repository.login(username, password);
    }

    public LiveData<String> register(String username, String password, String employeeId) {
        return repository.register(username, password, employeeId);
    }

    public LiveData<List<FileModel>> getFiles(String username, boolean isAdmin) {
        return repository.getFiles(username, isAdmin);
    }

    public LiveData<List<UserWithCount>> getUploadedUsers() {
        return repository.getUploadedUsers();
    }

    public LiveData<String> deleteFile(String fileId) {
        return repository.deleteFile(fileId);
    }

    public LiveData<String> deleteUserFolder(String username) {
        return repository.deleteUserFolder(username);
    }

    public LiveData<String> uploadFile(MultipartBody.Part file, RequestBody username) {
        return repository.uploadFile(file, username);
    }
}
