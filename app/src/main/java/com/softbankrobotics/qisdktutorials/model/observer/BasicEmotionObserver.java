package com.softbankrobotics.qisdktutorials.model.observer;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Emotion;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.qisdktutorials.model.data.BasicEmotion;
import com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics.OnBasicEmotionChangedListener;

import java.util.List;

/**
 * Observe the basic emotion of the first human seen by the robot.
 */
public class BasicEmotionObserver {

    // Store the basic emotion listener.
    private OnBasicEmotionChangedListener listener;
    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;
    // Store the observed human.
    private Human observedHuman;
    // Store the last excitement, pleasure and basic emotion.
    private ExcitementState lastExcitement;
    private PleasureState lastPleasure;
    private BasicEmotion lastBasicEmotion;

    /**
     * Start the observation.
     * @param qiContext the qiContext
     */
    public void startObserving(QiContext qiContext) {
        // Get the HumanAwareness service.
        humanAwareness = qiContext.getHumanAwareness();

        // Retrieve the humans around and update the observed human.
        List<Human> humansAround = humanAwareness.getHumansAround();
        updateObservedHuman(humansAround);

        // Update the observed human when the humans around change.
        humanAwareness.setOnHumansAroundChangedListener(new HumanAwareness.OnHumansAroundChangedListener() {
            @Override
            public void onHumansAroundChanged(List<Human> humansAround) {
                updateObservedHuman(humansAround);
            }
        });
    }

    /**
     * Stop the observation.
     */
    public void stopObserving() {
        // Clear observed human.
        clearObservedHuman();

        // Remove listener on HumanAwareness.
        if (humanAwareness != null) {
            humanAwareness.setOnHumansAroundChangedListener(null);
            humanAwareness = null;
        }
    }

    /**
     * Set the listener.
     * @param listener the listener
     */
    public void setListener(OnBasicEmotionChangedListener listener) {
        this.listener = listener;
    }

    private void updateObservedHuman(List<Human> humansAround) {
        // Clear observed human.
        clearObservedHuman();

        if (!humansAround.isEmpty()) {
            // Update observed human.
            observedHuman = humansAround.get(0);

            // Get and store human excitement and pleasure.
            Emotion emotion = observedHuman.getEmotion();
            lastExcitement = emotion.getExcitement();
            lastPleasure = emotion.getPleasure();

            // Notify the listener.
            notifyListener();

            // Notify the listener when excitement changes.
            emotion.setOnExcitementChangedListener(new Emotion.OnExcitementChangedListener() {
                @Override
                public void onExcitementChanged(ExcitementState excitementState) {
                    if (excitementState != lastExcitement) {
                        lastExcitement = excitementState;
                        notifyListener();
                    }
                }
            });

            // Notify the listener when pleasure changes.
            emotion.setOnPleasureChangedListener(new Emotion.OnPleasureChangedListener() {
                @Override
                public void onPleasureChanged(PleasureState pleasureState) {
                    if (pleasureState != lastPleasure) {
                        lastPleasure = pleasureState;
                        notifyListener();
                    }
                }
            });
        }
    }

    private void clearObservedHuman() {
        // Remove listeners on observed human.
        if (observedHuman != null) {
            Emotion emotion = observedHuman.getEmotion();
            emotion.setOnExcitementChangedListener(null);
            emotion.setOnPleasureChangedListener(null);
            observedHuman = null;
        }
    }

    private BasicEmotion computeBasicEmotion(ExcitementState excitement, PleasureState pleasure) {
        if (excitement == ExcitementState.UNKNOWN || pleasure == PleasureState.UNKNOWN) {
            return BasicEmotion.UNKNOWN;
        }

        switch (pleasure) {
            case POSITIVE:
                return (excitement == ExcitementState.CALM) ? BasicEmotion.CONTENT : BasicEmotion.JOYFUL;
            case NEGATIVE:
                return (excitement == ExcitementState.CALM) ? BasicEmotion.SAD : BasicEmotion.ANGRY;
        }

        return BasicEmotion.NEUTRAL;
    }

    private void notifyListener() {
        // Compute the basic emotion.
        BasicEmotion basicEmotion = computeBasicEmotion(lastExcitement, lastPleasure);
        // Notify the listener only if the basic emotion changed.
        if (basicEmotion != lastBasicEmotion) {
            lastBasicEmotion = basicEmotion;
            if (listener != null) {
                listener.onBasicEmotionChanged(basicEmotion);
            }
        }
    }
}
