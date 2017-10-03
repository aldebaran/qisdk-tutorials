package com.softbankrobotics.qisdktutorials.ui.conversation;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the conversation view.
 */
class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    private static final int LOG_VIEW_TYPE = 0;
    private static final int ROBOT_OUTPUT_VIEW_TYPE = 1;
    private static final int HUMAN_INPUT_VIEW_TYPE = 2;

    private List<ConversationItem> items = new ArrayList<>();

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = layoutFromViewType(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        ConversationItem conversationItem = items.get(position);
        holder.bind(conversationItem.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        ConversationItem conversationItem = items.get(position);
        ConversationItemType type = conversationItem.getType();
        switch (type) {
            case INFO_LOG:
            case ERROR_LOG:
                return LOG_VIEW_TYPE;
            case HUMAN_INPUT:
                return HUMAN_INPUT_VIEW_TYPE;
            case ROBOT_OUTPUT:
                return ROBOT_OUTPUT_VIEW_TYPE;
            default:
                throw new IllegalArgumentException("Unknown conversation item type: " + type);
        }
    }

    /**
     * Add an item to the view.
     * @param text the item text
     * @param type the item type
     */
    void addItem(String text, ConversationItemType type) {
        items.add(new ConversationItem(text, type));
        notifyItemInserted(items.size() - 1);
    }

    @LayoutRes
    private int layoutFromViewType(int viewType) {
        switch (viewType) {
            case LOG_VIEW_TYPE:
                return R.layout.layout_log_view;
            case ROBOT_OUTPUT_VIEW_TYPE:
                return R.layout.layout_robot_output_view;
            case HUMAN_INPUT_VIEW_TYPE:
                return R.layout.layout_human_input_view;
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }
}
