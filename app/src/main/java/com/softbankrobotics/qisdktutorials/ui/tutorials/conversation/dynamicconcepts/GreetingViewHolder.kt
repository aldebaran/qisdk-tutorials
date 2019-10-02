/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.greeting_layout.view.*

/**
 * The view holder to show a greeting.
 */
internal class GreetingViewHolder(itemView: View, private val onGreetingRemovedListener: OnGreetingRemovedListener?) : RecyclerView.ViewHolder(itemView) {

    /**
     * Binds a tutorial to the views.
     * @param greeting the greeting
     */
    fun bind(greeting: String) {
        itemView.greeting_textview.text = greeting
        itemView.delete_button.setOnClickListener { onGreetingRemovedListener?.onGreetingRemoved(greeting) }
    }
}
