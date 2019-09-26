/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.gotoframe

import android.os.Bundle
import android.util.Log
import com.aldebaran.qi.Future

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.GoToBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.aldebaran.qi.sdk.`object`.actuation.GoTo
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "GoToTutorialActivity"

/**
 * The activity for the GoTo tutorial.
 */
class GoToTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // Store the GoTo action.
    private var goTo: GoTo? = null

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
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can move around: I will go 1 meter forward.")
                .build()

        say.run()

        // Get the Actuation service from the QiContext.
        val actuation = qiContext.actuation

        // Get the robot frame.
        val robotFrame = actuation.robotFrame()

        // Create a transform corresponding to a 1 meter forward translation.
        val transform = TransformBuilder.create()
                .fromXTranslation(1.0)

        // Get the Mapping service from the QiContext.
        val mapping = qiContext.mapping

        // Create a FreeFrame with the Mapping service.
        val targetFrame = mapping.makeFreeFrame()

        // Update the target location relatively to Pepper's current location.
        targetFrame.update(robotFrame, transform, 0L)

        // Create a GoTo action.
        val goTo = GoToBuilder.with(qiContext) // Create the builder with the QiContext.
                .withFrame(targetFrame.frame()) // Set the target frame.
                .build() // Build the GoTo action.

        // Add an on started listener on the GoTo action.
        goTo.addOnStartedListener {
            val message = "GoTo action started."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
        }
        this.goTo = goTo

        // Execute the GoTo action asynchronously.
        val goToFuture: Future<Void> = goTo.async().run()

        // Add a lambda to the action execution.
        goToFuture.thenConsume { future ->
            if (future.isSuccess) {
                val message = "GoTo action finished with success."
                Log.i(TAG, message)
                displayLine(message, ConversationItemType.INFO_LOG)
            } else if (future.hasError()) {
                val message = "GoTo action finished with error."
                Log.e(TAG, message, future.error)
                displayLine(message, ConversationItemType.ERROR_LOG)
            }
        }
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove on started listeners from the GoTo action.
        goTo?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView?.addLine(text, type) }
    }
}
