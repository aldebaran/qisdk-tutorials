package com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics;

import com.softbankrobotics.qisdktutorials.model.data.BasicEmotion;

/**
 * Listener used to notify when the basic emotion changes.
 */
public interface OnBasicEmotionChangedListener {
    void onBasicEmotionChanged(BasicEmotion basicEmotion);
}
