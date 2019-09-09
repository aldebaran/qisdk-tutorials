/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

import com.softbankrobotics.qisdktutorials.R

/**
 * The view holder to show a greeting.
 */
internal class GreetingViewHolder(itemView: View, private val onGreetingRemovedListener: OnGreetingRemovedListener?) : RecyclerView.ViewHolder(itemView) {

    private val textView: TextView = itemView.findViewById(R.id.greeting_textview)
    private val button: ImageButton = itemView.findViewById(R.id.delete_button)

    /**
     * Binds a tutorial to the views.
     * @param greeting the greeting
     */
    fun bind(greeting: String) {
        textView.text = greeting
        button.setOnClickListener {onGreetingRemovedListener?.onGreetingRemoved(greeting)}
    }
}
