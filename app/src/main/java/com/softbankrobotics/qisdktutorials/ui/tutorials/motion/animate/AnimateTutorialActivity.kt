/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animate

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.conversation_layout.*

private const val TAG = "AnimateTutorialActivity"

/**
 * The activity for the Animate tutorial (animation).
 */
class AnimateTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null
    private var mediaPlayer: MediaPlayer? = null

    // Store the Animate action.
    private var animate: Animate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(this, R.raw.elephant_sound)
    }

    override fun onStop() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onStop()
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.conversation_layout

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can perform animations: here is an elephant.")
                .build()

        say.run()

        // Create an animation.
        val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.elephant_a001) // Set the animation resource.
                .build() // Build the animation.

        // Create an animate action.
        val animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build() // Build the animate action.
                .also { this.animate = it }

        // Add an on started listener to the animate action.
        animate.addOnStartedListener {
            val message = "Animation started."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)

            mediaPlayer?.start()
        }
        this.animate = animate

        // Run the animate action asynchronously.
        val animateFuture = animate.async().run()

        // Add a lambda to the action execution.
        animateFuture.thenConsume { future ->
            if (future.isSuccess) {
                val message = "Animation finished with success."
                Log.i(TAG, message)
                displayLine(message, ConversationItemType.INFO_LOG)
            } else if (future.hasError()) {
                val message = "Animation finished with error."
                Log.e(TAG, message, future.error)
                displayLine(message, ConversationItemType.ERROR_LOG)
            }
        }
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove on started listeners from the animate action.
        animate?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversation_view.addLine(text, type) }
    }

}
