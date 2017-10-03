package com.softbankrobotics.qisdktutorials.ui.categories;

import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

/**
 * The view holder to show a tutorial.
 */
class TutorialViewHolder extends RecyclerView.ViewHolder {

    private final RadioButton bubble;
    private final ImageView levelView;

    private OnTutorialClickedListener onTutorialClickedListener;

    TutorialViewHolder(View itemView, OnTutorialClickedListener onTutorialClickedListener) {
        super(itemView);
        this.onTutorialClickedListener = onTutorialClickedListener;
        bubble = itemView.findViewById(R.id.bubble);
        levelView = itemView.findViewById(R.id.levelView);
    }

    /**
     * Binds a tutorial to the views.
     * @param tutorial the tutorial to bind
     */
    void bind(final Tutorial tutorial) {
        bubble.setChecked(tutorial.isSelected());
        bubble.setEnabled(tutorial.isEnabled());
        bubble.setText("\"" + itemView.getContext().getString(tutorial.getNameResId()) + "\"");

        bubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTutorialClickedListener != null) {
                    onTutorialClickedListener.onTutorialClicked(tutorial);
                }
            }
        });

        TutorialLevel tutorialLevel = tutorial.getTutorialLevel();
        @DrawableRes int imageRes = imageResForTutorialLevel(tutorialLevel);
        levelView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), imageRes));
    }

    /**
     * Provide the image resource identifier for the specified tutorial level.
     * @param tutorialLevel the tutorial level
     * @return The image resource identifier.
     */
    @DrawableRes
    private int imageResForTutorialLevel(TutorialLevel tutorialLevel) {
        @DrawableRes int imageRes;
        switch (tutorialLevel) {
            case BASICS:
                imageRes = R.drawable.ic_img_btn_statusgreen;
                break;
            case ADVANCED:
                imageRes = R.drawable.ic_img_btn_statusorange;
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + tutorialLevel);
        }

        return imageRes;
    }
}
