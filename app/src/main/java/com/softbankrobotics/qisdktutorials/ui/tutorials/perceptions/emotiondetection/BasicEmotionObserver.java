package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.emotiondetection;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Emotion;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;

import java.util.List;

/**
 * Observe the basic emotion of the first human seen by the robot.
 */
public class BasicEmotionObserver {

    // Store the basic emotion listener.
    private OnBasicEmotionChangedListener listener;
    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;
    // Store the observed emotion.
    private Emotion observedEmotion;
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

        // Retrieve the humans around and update the observed emotion.
        List<Human> humansAround = humanAwareness.getHumansAround();
        updateObservedEmotion(humansAround);

        // Update the observed emotion when the humans around change.
        humanAwareness.addOnHumansAroundChangedListener(new HumanAwareness.OnHumansAroundChangedListener() {
            @Override
            public void onHumansAroundChanged(List<Human> humansAround) {
                updateObservedEmotion(humansAround);
            }
        });
    }

    /**
     * Stop the observation.
     */
    public void stopObserving() {
        // Clear observed emotion.
        clearObservedEmotion();

        // Remove listener on HumanAwareness.
        if (humanAwareness != null) {
            humanAwareness.removeAllOnHumansAroundChangedListeners();
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

    private void updateObservedEmotion(List<Human> humansAround) {
        // Clear observed emotion.
        clearObservedEmotion();

        if (!humansAround.isEmpty()) {
            // Update observed emotion.
            Human observedHuman = humansAround.get(0);
            observedEmotion = observedHuman.getEmotion();

            // Get and store human excitement and pleasure.
            lastExcitement = observedEmotion.getExcitement();
            lastPleasure = observedEmotion.getPleasure();

            // Notify the listener.
            notifyListener();

            // Notify the listener when excitement changes.
            observedEmotion.addOnExcitementChangedListener(new Emotion.OnExcitementChangedListener() {
                @Override
                public void onExcitementChanged(ExcitementState excitementState) {
                    if (excitementState != lastExcitement) {
                        lastExcitement = excitementState;
                        notifyListener();
                    }
                }
            });

            // Notify the listener when pleasure changes.
            observedEmotion.addOnPleasureChangedListener(new Emotion.OnPleasureChangedListener() {
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

    private void clearObservedEmotion() {
        // Remove listeners on observed emotion.
        if (observedEmotion != null) {
            observedEmotion.removeAllOnExcitementChangedListeners();
            observedEmotion.removeAllOnPleasureChangedListeners();
            observedEmotion = null;
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
