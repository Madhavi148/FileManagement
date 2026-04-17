package in.co.visiontek.filemanagement.data.model;

import com.google.gson.annotations.SerializedName;

public class FileModel {
    @SerializedName("_id")
    private String id;
    private String fileName;
    private String url;
    private long size;

    public FileModel(String id, String fileName, String url, long size) {
        this.id = id;
        this.fileName = fileName;
        this.url = url;
        this.size = size;
    }

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getUrl() { return url; }
    public long getSize() { return size; }
}
