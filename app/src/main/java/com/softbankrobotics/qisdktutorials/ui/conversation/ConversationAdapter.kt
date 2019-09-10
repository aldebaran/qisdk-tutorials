/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.conversation

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.softbankrobotics.qisdktutorials.R

private const val INFO_LOG_VIEW_TYPE = 0
private const val ERROR_LOG_VIEW_TYPE = 1
private const val ROBOT_OUTPUT_VIEW_TYPE = 2
private const val HUMAN_INPUT_VIEW_TYPE = 3

/**
 * Adapter for the conversation view.
 */
internal class ConversationAdapter : RecyclerView.Adapter<ConversationViewHolder>() {

    private val items = mutableListOf<ConversationItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val layout = layoutFromViewType(viewType)
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversationItem = items[position]
        holder.bind(conversationItem.text)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        val conversationItem = items[position]
        return when (val type = conversationItem.type) {
            ConversationItemType.INFO_LOG -> INFO_LOG_VIEW_TYPE
            ConversationItemType.ERROR_LOG -> ERROR_LOG_VIEW_TYPE
            ConversationItemType.HUMAN_INPUT -> HUMAN_INPUT_VIEW_TYPE
            ConversationItemType.ROBOT_OUTPUT -> ROBOT_OUTPUT_VIEW_TYPE
            else -> throw IllegalArgumentException("Unknown conversation item type: $type")
        }
    }

    /**
     * Add an item to the view.
     * @param text the item text
     * @param type the item type
     */
    fun addItem(text: String, type: ConversationItemType) {
        items.add(ConversationItem(text, type))
        notifyItemInserted(items.size - 1)
    }

    @LayoutRes
    private fun layoutFromViewType(viewType: Int): Int {
        return when (viewType) {
            INFO_LOG_VIEW_TYPE -> R.layout.layout_info_log_view
            ERROR_LOG_VIEW_TYPE -> R.layout.layout_error_log_view
            ROBOT_OUTPUT_VIEW_TYPE -> R.layout.layout_robot_output_view
            HUMAN_INPUT_VIEW_TYPE -> R.layout.layout_human_input_view
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
}
