package com.zybooks.projecttwo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ItemViewHolder> {
    private List<InventoryItem> items;
    private OnItemClickListener clickListener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(InventoryItem item, int position);
    }


    public InventoryAdapter() {
        this.items = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setItems(List<InventoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final TextView itemQuantity;
        private final TextView itemLocation;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            Button deleteButton = itemView.findViewById(R.id.buttonDeleteItem);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                //ignore clicks when Viewholder no longer holds a valid adapter position
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(items.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick(items.get(position), position);
                }
            });
        }

        public void bind(InventoryItem item) {
            itemName.setText(itemView.getContext().getString(R.string.item_number_label, item.getItemNumber()));
            itemQuantity.setText(itemView.getContext().getString(R.string.quantity_label, item.getQuantity()));
            itemLocation.setText(itemView.getContext().getString(R.string.location_label, item.getLocation()));
        }
    }
}