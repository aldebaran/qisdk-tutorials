/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import android.support.v7.widget.RecyclerView
import android.view.View

import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel
import kotlinx.android.synthetic.main.tutorial_layout.view.*

/**
 * The view holder to show a tutorial.
 */
internal class TutorialViewHolder(itemView: View, private val onTutorialClickedListener: OnTutorialClickedListener?) : RecyclerView.ViewHolder(itemView) {

    /**
     * Binds a tutorial to the views.
     * @param tutorial the tutorial to bind
     */
    fun bind(tutorial: Tutorial) {
        itemView.radioButton.isChecked = tutorial.isSelected
        itemView.radioButton.isEnabled = tutorial.isEnabled
        itemView.radioButton.text = "\"" + itemView.context.getString(tutorial.nameResId) + "\""

        itemView.radioButton.setOnClickListener {
            onTutorialClickedListener?.onTutorialClicked(tutorial)
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
                itemView.levelTextView.setText(R.string.basic_level)
                itemView.levelTextView.setBackgroundResource(R.drawable.basic_level_shape)
            }
            TutorialLevel.ADVANCED -> {
                itemView.levelTextView.setText(R.string.advanced_level)
                itemView.levelTextView.setBackgroundResource(R.drawable.advanced_level_shape)
            }
        }
    }
}
