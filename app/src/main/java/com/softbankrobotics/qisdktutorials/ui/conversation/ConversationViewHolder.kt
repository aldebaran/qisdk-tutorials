/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.conversation

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.softbankrobotics.qisdktutorials.R

/**
 * View holder for the conversation view.
 */
internal class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val textView: TextView = itemView.findViewById(R.id.textView)

    /**
     * Bind the text to the view.
     * @param text the text
     */
    fun bind(text: String) {
        textView.text = text
    }
}
