package in.co.visiontek.filemanagement.data.model;

public class UserWithCount {
    private String username;
    private int fileCount;

    public UserWithCount(String username, int fileCount) {
        this.username = username;
        this.fileCount = fileCount;
    }

    public String getUsername() {
        return username;
    }

    public int getFileCount() {
        return fileCount;
    }
}
