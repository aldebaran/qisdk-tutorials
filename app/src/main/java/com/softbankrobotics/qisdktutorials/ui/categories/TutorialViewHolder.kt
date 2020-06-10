/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel
import kotlinx.android.synthetic.main.tutorial_layout.view.*

/**
 * The view holder to show a tutorial.
 */
internal class TutorialViewHolder(itemView: View, private val onTutorialClickedListener: OnTutorialClickedListener) : RecyclerView.ViewHolder(itemView) {

    /**
     * Binds a tutorial to the views.
     * @param tutorial the tutorial to bind
     */
    fun bind(tutorial: Tutorial) {

        with(itemView.radio_button) {
            isChecked = tutorial.isSelected
            isEnabled = tutorial.isEnabled
            text = "\"${itemView.context.getString(tutorial.nameResId)}\""
            setOnClickListener {
                onTutorialClickedListener.onTutorialClicked(tutorial)
            }
        }

        val tutorialLevel = tutorial.tutorialLevel
        bindLevelView(tutorialLevel)
    }

    /**
     * Bind the level view.
     * @param tutorialLevel the tutorial level
     */
    private fun bindLevelView(tutorialLevel: TutorialLevel) {
        when (tutorialLevel) {
            TutorialLevel.BASIC -> {
                itemView.level_textview.setText(R.string.basic_level)
                itemView.level_textview.setBackgroundResource(R.drawable.basic_level_shape)
            }
            TutorialLevel.ADVANCED -> {
                itemView.level_textview.setText(R.string.advanced_level)
                itemView.level_textview.setBackgroundResource(R.drawable.advanced_level_shape)
            }
        }
    }
}
