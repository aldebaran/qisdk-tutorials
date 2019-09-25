/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.emotiondetection

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.human.Emotion
import com.aldebaran.qi.sdk.`object`.human.ExcitementState
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.human.PleasureState
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import kotlin.properties.Delegates

/**
 * Observe the basic emotion of the first human seen by the robot.
 */
class BasicEmotionObserver {

    // Store the basic emotion listener.
    private var listener: OnBasicEmotionChangedListener? = null
    // Store the HumanAwareness service.
    private var humanAwareness: HumanAwareness? = null
    // Store the observed emotion.
    private var observedEmotion: Emotion? = null
    // Observe the last excitement state and notify the listener.
    private var lastExcitement by Delegates.observable(ExcitementState.UNKNOWN) { _, _, _ ->
        notifyListener()
    }
    // Observe the last pleasure state and notify the listener.
    private var lastPleasure by Delegates.observable(PleasureState.UNKNOWN) { _, _, _ ->
        notifyListener()
    }
    // Observe the last basic emotion state and change basic emotion.
    private var lastBasicEmotion by Delegates.observable(BasicEmotion.UNKNOWN) { _, _, new ->
        listener?.onBasicEmotionChanged(new)
    }

    /**
     * Start the observation.
     * @param qiContext the qiContext
     */
    fun startObserving(qiContext: QiContext) {
        // Get the HumanAwareness service.
        val humanAwareness = qiContext.humanAwareness

        // Retrieve the humans around and update the observed emotion.
        val humansAround = humanAwareness.humansAround
        updateObservedEmotion(humansAround)

        // Update the observed emotion when the humans around change.
        humanAwareness.addOnHumansAroundChangedListener { this.updateObservedEmotion(it) }

        this.humanAwareness = humanAwareness
    }

    /**
     * Stop the observation.
     */
    fun stopObserving() {
        // Clear observed emotion.
        clearObservedEmotion()

        // Remove listener on HumanAwareness.
        humanAwareness?.let {
            it.removeAllOnHumansAroundChangedListeners()
            humanAwareness = null
        }
    }

    /**
     * Set the listener.
     * @param listener the listener
     */
    fun setListener(listener: OnBasicEmotionChangedListener?) {
        this.listener = listener
    }

    private fun updateObservedEmotion(humansAround: List<Human>) {
        // Clear observed emotion.
        clearObservedEmotion()

        if (humansAround.isNotEmpty()) {
            // Update observed emotion.
            val observedHuman = humansAround[0]
            val observedEmotion = observedHuman.emotion

            // Get and store human excitement and pleasure.
            lastExcitement = observedEmotion.excitement
            lastPleasure = observedEmotion.pleasure

            // Notify the listener when excitement changes.
            observedEmotion.addOnExcitementChangedListener { excitementState ->
                if (excitementState != lastExcitement) {
                    lastExcitement = excitementState
                }
            }

            // Notify the listener when pleasure changes.
            observedEmotion.addOnPleasureChangedListener { pleasureState ->
                if (pleasureState != lastPleasure) {
                    lastPleasure = pleasureState
                }
            }

            this.observedEmotion = observedEmotion
        }
    }

    private fun clearObservedEmotion() {
        // Remove listeners on observed emotion.
        observedEmotion?.let {
            it.removeAllOnExcitementChangedListeners()
            it.removeAllOnPleasureChangedListeners()
            observedEmotion = null
        }

    }

    private fun computeBasicEmotion(excitement: ExcitementState, pleasure: PleasureState): BasicEmotion {
        if (excitement == ExcitementState.UNKNOWN) {
            return BasicEmotion.UNKNOWN
        }

        return when (pleasure) {
            PleasureState.UNKNOWN -> BasicEmotion.UNKNOWN
            PleasureState.NEUTRAL -> BasicEmotion.NEUTRAL
            PleasureState.POSITIVE -> if (excitement == ExcitementState.CALM) BasicEmotion.CONTENT else BasicEmotion.JOYFUL
            PleasureState.NEGATIVE -> if (excitement == ExcitementState.CALM) BasicEmotion.SAD else BasicEmotion.ANGRY
        }
    }

    private fun notifyListener() {
        // Compute the basic emotion.
        val basicEmotion = computeBasicEmotion(lastExcitement, lastPleasure)
        // Changing only if the basic emotion changed.
        if (basicEmotion != lastBasicEmotion) {
            lastBasicEmotion = basicEmotion
        }
    }
}
