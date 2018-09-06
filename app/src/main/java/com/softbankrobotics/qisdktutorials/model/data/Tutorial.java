/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.model.data;

import android.support.annotation.StringRes;

/**
 * Represents a tutorial.
 */
public class Tutorial {

    private final TutorialId id;
    private final @StringRes int nameResId;
    private final String qiChatbotId;
    private final TutorialLevel tutorialLevel;
    private boolean selected;
    private boolean enabled;

    public Tutorial(TutorialId id, @StringRes int nameResId, String qiChatbotId, TutorialLevel tutorialLevel) {
        this.id = id;
        this.nameResId = nameResId;
        this.qiChatbotId = qiChatbotId;
        this.tutorialLevel = tutorialLevel;
        this.selected = false;
        this.enabled = true;
    }

    public @StringRes int getNameResId() {
        return nameResId;
    }

    public String getQiChatbotId() {
        return qiChatbotId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TutorialLevel getTutorialLevel() {
        return tutorialLevel;
    }

    public TutorialId getId() {
        return id;
    }
}
