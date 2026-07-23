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
    //list of all inventory items
    private List<InventoryItemActivity> items;
    //onclick listener
    private OnItemClickListener clickListener;
    //on delete click listener
    private OnDeleteClickListener deleteListener;

    //this is how the inventory activity can react to a item card being clicked
    public interface OnItemClickListener {
        void onItemClick(InventoryItemActivity item);
    }

    //this is how the inventory activity can react to a delete button being clicked on an item card
    public interface OnDeleteClickListener {
        void onDeleteClick(InventoryItemActivity item, int position);
    }

    //start out with an empty list
    public InventoryAdapter() {
        this.items = new ArrayList<>();
    }

    //setter for the  onclick listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    //setter for the onclick listener
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    //updates the current list of items
    public void setItems(List<InventoryItemActivity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    //removes an item from the list and updates
    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    //view holder for each item
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    //binds the data from each item to the view holder
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InventoryItemActivity item = items.get(position);
        holder.bind(item);
    }

    //returns the total items to display
    @Override
    public int getItemCount() {
        return items.size();
    }

    //this class shows how each card will behave and the layout of it
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

            // Set up click listener for the entire card
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(items.get(position));
                }
            });

            // Set up click listener for delete button
            deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick(items.get(position), position);
                }
            });
        }

        //binds the item data to the inventory card
        public void bind(InventoryItemActivity item) {
            itemName.setText("Item #: " + item.getItemNumber());
            itemQuantity.setText("Quantity: " + item.getQuantity());
            itemLocation.setText("Location: " + item.getLocation());
        }
    }
}
