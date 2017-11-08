package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;

/**
 * The view holder to show an item.
 */
class ItemViewHolder extends RecyclerView.ViewHolder {

    private TextView textView;
    private Button button;

    private OnItemRemovedListener onItemRemovedListener;

    ItemViewHolder(View itemView, OnItemRemovedListener onItemRemovedListener) {
        super(itemView);
        this.onItemRemovedListener = onItemRemovedListener;
        textView = itemView.findViewById(R.id.item_name_textview);
        button = itemView.findViewById(R.id.delete_button);
    }

    /**
     * Binds a tutorial to the views.
     * @param itemName the item name
     */
    void bind(final String itemName) {
        textView.setText(itemName);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemRemovedListener != null) {
                    onItemRemovedListener.onItemRemoved(itemName);
                }
            }
        });
    }
}
