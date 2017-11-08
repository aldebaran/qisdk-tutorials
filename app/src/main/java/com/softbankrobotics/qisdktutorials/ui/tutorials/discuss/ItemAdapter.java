package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter used to show items.
 */
class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<String> items;
    private OnItemRemovedListener onItemRemovedListener;

    ItemAdapter(OnItemRemovedListener onItemRemovedListener) {
        this.items = new ArrayList<>();
        this.onItemRemovedListener = onItemRemovedListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view, onItemRemovedListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String itemName = items.get(position);
        holder.bind(itemName);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void addItem(String itemName) {
        items.add(itemName);
        notifyItemInserted(items.size() - 1);
    }

    void removeItem(String itemName) {
        int index = items.indexOf(itemName);
        if (index != -1) {
            items.remove(itemName);
            notifyItemRemoved(index);
        }
    }

    boolean containsItem(String itemName) {
        return items.contains(itemName);
    }
}
