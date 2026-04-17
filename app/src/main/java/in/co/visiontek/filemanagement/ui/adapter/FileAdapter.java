package in.co.visiontek.filemanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.co.visiontek.filemanagement.data.model.FileModel;
import in.co.visiontek.filemanagement.databinding.ItemFileBinding;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileModel> files;
    private final OnFileClickListener listener;

    public interface OnFileClickListener {
        void onDownloadClick(FileModel file);
        void onQRClick(FileModel file);
        void onDeleteClick(FileModel file);
    }

    public FileAdapter(List<FileModel> files, OnFileClickListener listener) {
        this.files = files;
        this.listener = listener;
    }

    public void setFiles(List<FileModel> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileBinding binding = ItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel file = files.get(position);
        holder.binding.tvFileName.setText(file.getFileName());
        holder.binding.tvFileSize.setText(formatFileSize(file.getSize()));
        
        holder.binding.btnDownload.setOnClickListener(v -> listener.onDownloadClick(file));
        holder.binding.btnQR.setOnClickListener(v -> listener.onQRClick(file));
        
        if (holder.binding.btnDelete != null) {
            holder.binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(file));
        }
    }

    @Override
    public int getItemCount() {
        return files != null ? files.size() : 0;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        public ItemFileBinding binding;

        public FileViewHolder(ItemFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
