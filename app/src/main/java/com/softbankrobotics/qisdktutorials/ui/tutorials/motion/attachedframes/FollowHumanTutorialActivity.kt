/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.attachedframes

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.GoToBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.actuation.GoTo
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.util.FutureUtils
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.conversationView
import kotlinx.android.synthetic.main.activity_follow_human_tutorial.*

import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

private const val TAG = "FollowHumanActivity"

class FollowHumanTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null
    // Store the action execution future.
    private var goToFuture: Future<Void>? = null
    // Store the GoTo action.
    private var goTo: GoTo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Search humans on follow button clicked.
        follow_button.setOnClickListener {
            if (qiContext != null) {
                follow_button.isEnabled = false
                displayLine("Following in 3 seconds...", ConversationItemType.INFO_LOG)
                // Wait 3 seconds before following.
                FutureUtils.wait(3, TimeUnit.SECONDS).andThenConsume { searchHumans() }
            }
        }

        // Stop moving on stop button clicked.
        stop_button.setOnClickListener {
            stop_button.isEnabled = false
            val message = "Stopping..."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
            stopMoving()
        }

        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        QiSDK.unregister(this, this)

        super.onDestroy()
    }

    override val layoutId = R.layout.activity_follow_human_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        Log.i(TAG, "Focus gained.")
        // Store the provided QiContext.
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("Press \"Follow\" and I will follow you. Press \"Stop\" to stop me.")
                .build()

        say.run()

        enterWaitingForOrderState()
    }

    override fun onRobotFocusLost() {
        Log.i(TAG, "Focus lost.")

        conversationBinder?.unbind()

        // Remove the QiContext.
        this.qiContext = null
        // Remove on started listeners from the GoTo action.
        goTo?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun enterWaitingForOrderState() {
        val message = "Waiting for order..."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)
        runOnUiThread {
            stop_button.isEnabled = false
            follow_button.isEnabled = true
        }
    }

    private fun enterMovingState() {
        val message = "Moving..."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)
        runOnUiThread {
            follow_button.isEnabled = false
            stop_button.isEnabled = true
        }
    }

    private fun searchHumans() {
        val qiContext = qiContext
        if(qiContext != null) {
            val humanAwareness = qiContext.humanAwareness
            val humansAroundFuture = humanAwareness.async().humansAround
            humansAroundFuture.andThenConsume { humans ->
                // If humans found, follow the closest one.
                if (humans.isNotEmpty()) {
                    Log.i(TAG, "Human found.")
                    val humanToFollow = getClosestHuman(humans, qiContext)
                    humanToFollow?.let { followHuman(it) }
                } else {
                    Log.i(TAG, "No human.")
                    enterWaitingForOrderState()
                }
            }
        }
    }

    private fun followHuman(human: Human) {
        // Create the target frame from the human.
        val targetFrame = createTargetFrame(human)

        // Create a GoTo action.
        val goTo = GoToBuilder.with(qiContext)
                .withFrame(targetFrame)
                .build()
                .also { this.goTo = it }

        // Update UI when the GoTo action starts.
        goTo.addOnStartedListener {
            this.enterMovingState()
        }

        this.goTo = goTo

        // Execute the GoTo action asynchronously.
        val goToFuture = goTo.async().run()

        // Update UI when the GoTo action finishes.
        goToFuture.thenConsume { future ->
            when {
                future.isSuccess -> {
                    Log.i(TAG, "Target reached.")
                    enterWaitingForOrderState()
                }
                future.isCancelled -> {
                    Log.i(TAG, "Movement stopped.")
                    enterWaitingForOrderState()
                }
                else -> {
                    Log.e(TAG, "Movement error.", future.error)
                    enterWaitingForOrderState()
                }
            }
        }
        this.goToFuture = goToFuture
    }

    private fun createTargetFrame(humanToFollow: Human): Frame {
        // Get the human head frame.
        val humanFrame = humanToFollow.headFrame
        // Create a transform for Pepper to stay at 1 meter in front of the human.
        val transform = TransformBuilder.create().fromXTranslation(1.0)
        // Create an AttachedFrame that automatically updates with the human frame.
        val attachedFrame = humanFrame.makeAttachedFrame(transform)
        // Returns the corresponding Frame.
        return attachedFrame.frame()
    }

    private fun stopMoving() {
        // Cancel the GoTo action asynchronously.
        goToFuture?.requestCancellation()
    }

    private fun getClosestHuman(humans: List<Human>, qiContext: QiContext): Human? {
        // Get the robot frame.
        val robotFrame = qiContext.actuation.robotFrame()

        // Return the closest human
        return humans.minBy {
            getDistance(robotFrame, it)
        }
    }

    private fun getDistance(robotFrame: Frame, human: Human): Double {
        // Get the human head frame.
        val humanFrame = human.headFrame
        // Retrieve the translation between the robot and the human.
        val translation = humanFrame.computeTransform(robotFrame).transform.translation
        // Get the translation coordinates.
        val x = translation.x
        val y = translation.y
        // Compute and return the distance.
        return sqrt(x * x + y * y)
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

}
