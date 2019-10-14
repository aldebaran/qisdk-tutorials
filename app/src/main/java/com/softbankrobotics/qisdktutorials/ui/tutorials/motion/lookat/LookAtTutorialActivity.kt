/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.lookat

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.LookAtBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.aldebaran.qi.sdk.`object`.actuation.LookAt
import com.aldebaran.qi.sdk.`object`.actuation.LookAtMovementPolicy
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_look_at_tutorial.*

private const val TAG = "LookAtTutorialActivity"

/**
 * The activity for the LookAt tutorial.
 */
class LookAtTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // Store the LookAt action.
    private var lookAt: LookAt? = null

    // Store the action execution future.
    private var lookAtFuture: Future<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the button onClick listener.
        cancel_button.setOnClickListener {
            lookAtFuture?.requestCancellation()
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_look_at_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can look at things: I will look at the ground.")
                .build()

        say.run()

        // Get the Actuation service from the QiContext.
        val actuation = qiContext.actuation

        // Get the robot frame.
        val robotFrame = actuation.robotFrame()

        // Create a transform corresponding to a 1 meter forward translation.
        val transform = TransformBuilder.create()
                .from2DTranslation(1.0, 0.0)

        // Get the Mapping service from the QiContext.
        val mapping = qiContext.mapping

        // Create a FreeFrame with the Mapping service.
        val targetFrame = mapping.makeFreeFrame()

        // Update the target location relatively to Pepper's current location.
        targetFrame.update(robotFrame, transform, 0L)

        // Create a LookAt action.
        val lookAt = LookAtBuilder.with(qiContext) // Create the builder with the context.
                .withFrame(targetFrame.frame()) // Set the target frame.
                .build() // Build the LookAt action.
                .also { lookAt = it }

        // Set the LookAt policy to look with the head only.
        lookAt.policy = LookAtMovementPolicy.HEAD_ONLY

        // Add an on started listener on the LookAt action.
        lookAt.addOnStartedListener {
            val message = "LookAt action started."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
        }

        // Run the LookAt action asynchronously.
        val lookAtFuture = lookAt.async().run()

        this.lookAt = lookAt

        // Add a lambda to the action execution.
        lookAtFuture.thenConsume {
            when {
                it.isSuccess -> {
                    val message = "LookAt action finished with success."
                    Log.i(TAG, message)
                    displayLine(message, ConversationItemType.INFO_LOG)
                }
                it.isCancelled -> {
                    val message = "LookAt action was cancelled."
                    Log.i(TAG, message)
                    displayLine(message, ConversationItemType.INFO_LOG)
                }
                else -> {
                    val message = "LookAt action finished with error."
                    Log.e(TAG, message, it.error)
                    displayLine(message, ConversationItemType.ERROR_LOG)
                }
            }
        }
        this.lookAtFuture = lookAtFuture
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove on started listeners from the LookAt action.
        lookAt?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversation_view.addLine(text, type) }
    }
}
