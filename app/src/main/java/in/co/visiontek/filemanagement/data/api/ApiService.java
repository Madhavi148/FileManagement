package in.co.visiontek.filemanagement.data.api;

import java.util.List;
import in.co.visiontek.filemanagement.data.model.FileModel;
import in.co.visiontek.filemanagement.data.model.User;
import in.co.visiontek.filemanagement.data.model.UserWithCount;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("/api/auth/register")
    Call<ResponseBody> register(@Body User user);

    @POST("/api/auth/login")
    Call<ResponseBody> login(@Body User user);

    @GET("/api/files")
    Call<List<FileModel>> getFiles(@Query("username") String username, @Query("isAdmin") boolean isAdmin);

    @GET("/api/admin/users")
    Call<List<UserWithCount>> getUploadedUsers();

    @DELETE("/api/files/{id}")
    Call<ResponseBody> deleteFile(@Path("id") String fileId);

    @DELETE("/api/admin/users/{username}")
    Call<ResponseBody> deleteUserFolder(@Path("username") String username);

    @Multipart
    @POST("/api/files/upload")
    Call<ResponseBody> uploadFile(
            @Part MultipartBody.Part file,
            @Part("username") RequestBody username
    );

    @GET("/api/files/download/{fileName}")
    @Streaming
    Call<ResponseBody> downloadFile(@Path("fileName") String fileName);
}
