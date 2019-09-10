/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.enforcetabletreachability

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.EnforceTabletReachabilityBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.EnforceTabletReachability
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_enforce_tablet_reachability_tutorial.*

private const val TAG = "TabletReachActivity"

/**
 * The activity for the EnforceTabletReachability tutorial.
 */
class EnforceTabletReachabilityTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {
    private var conversationBinder: ConversationBinder? = null

    // Store qiContext
    private var qiContext: QiContext? = null

    // Store the action.
    private var enforceTabletReachability: EnforceTabletReachability? = null

    // Store action future
    private var enforceTabletReachabilityFuture: Future<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tablet_reachability_button.setOnClickListener {
            val enforceTabletReachabilityFuture = this.enforceTabletReachabilityFuture
            when {
                enforceTabletReachability == null -> {
                    val errorLog = "EnforceTabletReachability has not been built yet"
                    displayLine(errorLog, ConversationItemType.ERROR_LOG)
                    Log.e(TAG, errorLog)
                }
                enforceTabletReachabilityFuture == null || enforceTabletReachabilityFuture.isDone ->
                    // The EnforceTabletReachability action is not running
                    startEnforceTabletReachability()
                else -> // The EnforceTabletReachability action is running
                    stopEnforceTabletReachability()
            }
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    private fun stopEnforceTabletReachability() {
        enforceTabletReachabilityFuture?.requestCancellation()
    }

    private fun startEnforceTabletReachability() {
        // Run the action asynchronously
        enforceTabletReachabilityFuture = enforceTabletReachability?.async()?.run()

        // Handle the action's end
        enforceTabletReachabilityFuture?.thenConsume { future ->
            // Display eventual errors
            if (future.hasError()) {
                val message = "The EnforceTabletReachability action finished with error."
                Log.e(TAG, message, future.error)
                displayLine(message, ConversationItemType.ERROR_LOG)
            } else {
                val message = "The EnforceTabletReachability action has finished."
                Log.i(TAG, message)
                displayLine(message, ConversationItemType.INFO_LOG)
            }

            // Update button text
            setButtonText(resources.getString(R.string.enforce_tablet_reachability))

            // Say text when the action is cancelled
            val say = SayBuilder.with(qiContext)
                    .withText("My movements are back to normal. Run the action again to see the difference.")
                    .build()

            say.run()
        }
    }

    private fun setButtonText(str: String) {
        runOnUiThread { tablet_reachability_button.text = str }
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun getLayoutId(): Int = R.layout.activity_enforce_tablet_reachability_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store qiContext
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        // Build introduction Say
        val say = SayBuilder.with(qiContext)
                .withText("I can enforce my tablet reachability by limiting my movements. Try it out!")
                .build()

        say.run()

        // Build EnforceTabletReachability action.
        val enforceTabletReachability = EnforceTabletReachabilityBuilder
                .with(qiContext).build().also { this.enforceTabletReachability = it }

        // On started listener
        enforceTabletReachability.addOnStartedListener {
            // Display log
            val infoLog = "The EnforceTabletReachability action has started."
            displayLine(infoLog, ConversationItemType.INFO_LOG)
            Log.i(TAG, infoLog)
        }

        // On position reached listener
        enforceTabletReachability.addOnPositionReachedListener {
            // Display log
            val infoLog = "The tablet now is in position."
            displayLine(infoLog, ConversationItemType.INFO_LOG)
            Log.i(TAG, infoLog)

            // Update button text
            setButtonText(resources.getString(R.string.cancel_action))

            val s = SayBuilder.with(qiContext)
                    .withText("My movements are now limited. Cancel the action to see the difference.")
                    .build()

            s.run()
        }

        this.enforceTabletReachability = enforceTabletReachability

        enableButton()
    }

    private fun enableButton() {
        runOnUiThread { tablet_reachability_button.isEnabled = true }
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove all listeners
        enforceTabletReachability?.removeAllOnStartedListeners()
        enforceTabletReachability?.removeAllOnPositionReachedListeners()

        this.qiContext = null
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

}
