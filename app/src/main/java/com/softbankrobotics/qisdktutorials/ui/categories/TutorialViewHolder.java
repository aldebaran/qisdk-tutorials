/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

/**
 * The view holder to show a tutorial.
 */
class TutorialViewHolder extends RecyclerView.ViewHolder {

    private final RadioButton radioButton;
    private final TextView levelTextView;

    private OnTutorialClickedListener onTutorialClickedListener;

    TutorialViewHolder(View itemView, OnTutorialClickedListener onTutorialClickedListener) {
        super(itemView);
        this.onTutorialClickedListener = onTutorialClickedListener;
        radioButton = itemView.findViewById(R.id.radioButton);
        levelTextView = itemView.findViewById(R.id.levelTextView);
    }

    /**
     * Binds a tutorial to the views.
     * @param tutorial the tutorial to bind
     */
    void bind(final Tutorial tutorial) {
        radioButton.setChecked(tutorial.isSelected());
        radioButton.setEnabled(tutorial.isEnabled());
        radioButton.setText("\"" + itemView.getContext().getString(tutorial.getNameResId()) + "\"");

        radioButton.setOnClickListener(v -> {
            if (onTutorialClickedListener != null) {
                onTutorialClickedListener.onTutorialClicked(tutorial);
            }
        });

        TutorialLevel tutorialLevel = tutorial.getTutorialLevel();
        bindLevelView(tutorialLevel);
    }

    /**
     * Bind the level view.
     * @param tutorialLevel the tutorial level
     */
    private void bindLevelView(@NonNull TutorialLevel tutorialLevel) {
        switch (tutorialLevel) {
            case BASIC:
                levelTextView.setText(R.string.basic_level);
                levelTextView.setBackgroundResource(R.drawable.basic_level_shape);
                break;
            case ADVANCED:
                levelTextView.setText(R.string.advanced_level);
                levelTextView.setBackgroundResource(R.drawable.advanced_level_shape);
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + tutorialLevel);
        }
    }
}
