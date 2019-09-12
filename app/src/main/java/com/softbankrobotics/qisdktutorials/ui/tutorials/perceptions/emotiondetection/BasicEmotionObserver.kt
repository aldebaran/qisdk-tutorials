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
import com.aldebaran.qi.sdk.`object`.human.PleasureState.*
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness

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
    // Store the last excitement, pleasure and basic emotion.
    private var lastExcitement: ExcitementState? = null
    private var lastPleasure: PleasureState? = null
    private var lastBasicEmotion: BasicEmotion? = null

    /**
     * Start the observation.
     * @param qiContext the qiContext
     */
    fun startObserving(qiContext: QiContext) {
        // Get the HumanAwareness service.
        humanAwareness = qiContext.humanAwareness

        // Retrieve the humans around and update the observed emotion.
        val humansAround = humanAwareness!!.humansAround
        updateObservedEmotion(humansAround)

        // Update the observed emotion when the humans around change.
        humanAwareness?.addOnHumansAroundChangedListener { this.updateObservedEmotion(it) }
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
            observedEmotion = observedHuman.emotion

            // Get and store human excitement and pleasure.
            lastExcitement = observedEmotion?.excitement
            lastPleasure = observedEmotion?.pleasure

            // Notify the listener.
            notifyListener()

            // Notify the listener when excitement changes.
            observedEmotion?.addOnExcitementChangedListener { excitementState ->
                if (excitementState != lastExcitement) {
                    lastExcitement = excitementState
                    notifyListener()
                }
            }

            // Notify the listener when pleasure changes.
            observedEmotion?.addOnPleasureChangedListener { pleasureState ->
                if (pleasureState != lastPleasure) {
                    lastPleasure = pleasureState
                    notifyListener()
                }
            }
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

    private fun computeBasicEmotion(excitement: ExcitementState?, pleasure: PleasureState?): BasicEmotion {
        if (excitement == ExcitementState.UNKNOWN || pleasure == UNKNOWN) {
            return BasicEmotion.UNKNOWN
        }

        return when (pleasure) {
            POSITIVE -> if (excitement == ExcitementState.CALM) BasicEmotion.CONTENT else BasicEmotion.JOYFUL
            NEGATIVE -> if (excitement == ExcitementState.CALM) BasicEmotion.SAD else BasicEmotion.ANGRY
            UNKNOWN -> TODO()
            NEUTRAL -> TODO()
            null -> TODO()
        }
    }

    private fun notifyListener() {
        // Compute the basic emotion.
        val basicEmotion = computeBasicEmotion(lastExcitement, lastPleasure)
        // Notify the listener only if the basic emotion changed.
        if (basicEmotion !== lastBasicEmotion) {
            lastBasicEmotion = basicEmotion
            listener?.onBasicEmotionChanged(basicEmotion)
        }
    }
}
