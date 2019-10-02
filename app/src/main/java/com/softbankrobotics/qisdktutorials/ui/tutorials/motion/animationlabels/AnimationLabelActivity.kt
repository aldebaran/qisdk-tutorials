/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animationlabels

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
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity

private const val TAG = "AnimationLabelActivity"

class AnimationLabelActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var conversationView: ConversationView
    private var conversationBinder: ConversationBinder? = null

    // Store the Animate action.
    private var animate: Animate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationView = findViewById(R.id.conversation_view)

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
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can trigger events using animation labels: I will synchronize my speech with my dance.")
                .build()

        say.run()

        // Create an animation.
        val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.dance_b001) // Set the animation resource.
                .build() // Build the animation.

        // Create an animate action.
        val animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build() // Build the animate action.

        // Say and display the name of the reached labels
        animate.addOnLabelReachedListener { label, _ ->
            val sayLabel = SayBuilder.with(qiContext)
                    .withText(label)
                    .build()

            sayLabel.async().run()
        }

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

        // Remove the signal listeners from the animate action.
        animate?.removeAllOnStartedListeners()
        animate?.removeAllOnLabelReachedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

}