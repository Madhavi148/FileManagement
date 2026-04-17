package in.co.visiontek.filemanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.co.visiontek.filemanagement.data.model.UserWithCount;
import in.co.visiontek.filemanagement.databinding.ItemUserBinding;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserWithCount> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String username);
        void onDeleteFolderClick(String username);
    }

    public UserAdapter(List<UserWithCount> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void setUsers(List<UserWithCount> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserWithCount user = users.get(position);
        holder.binding.tvUsername.setText(user.getUsername());
        holder.binding.tvFileCount.setText(user.getFileCount() + " files");
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user.getUsername()));
        holder.binding.btnDeleteFolder.setOnClickListener(v -> listener.onDeleteFolderClick(user.getUsername()));
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
