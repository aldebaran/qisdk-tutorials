package com.softbankrobotics.qisdktutorials.ui.conversation;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;

/**
 * View holder for the conversation view.
 */
class ConversationViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;

    ConversationViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.textView);
    }

    /**
     * Bind the text to the view.
     * @param text the text
     */
    void bind(String text) {
        textView.setText(text);
    }
}
