/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.trajectory

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
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "TrajectoryActivity"

/**
 * The activity for the Animate tutorial (trajectory).
 */
class TrajectoryTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // Store the Animate action.
    private var animate: Animate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
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
                .withText("I can perform trajectories: I will move forward.")
                .build()

        say.run()

        // Create an animation.
        val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.dance) // Set the animation resource.
                .build() // Build the animation.

        // Create an animate action.
        val animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build() // Build the animate action.

        // Add an on started listener to the animate action.
        animate.addOnStartedListener {
            val message = "Animation started."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
        }

        // Run the animate action asynchronously.
        val animateFuture = animate.async().run()

        this.animate = animate

        // Add a lambda to the action execution.
        animateFuture.thenConsume {
            if (it.isSuccess) {
                val message = "Animation finished with success."
                Log.i(TAG, message)
                displayLine(message, ConversationItemType.INFO_LOG)
            } else if (it.hasError()) {
                val message = "Animation finished with error."
                Log.e(TAG, message, it.error)
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
