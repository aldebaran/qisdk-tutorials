/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.touch

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.touch.TouchSensor
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "TouchTutorialActivity"

/**
 * The activity for the Touch tutorial.
 */
class TouchTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // Store the head touch sensor.
    private var headTouchSensor: TouchSensor? = null

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

    override fun getLayoutId() = R.layout.conversation_layout

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I have touch sensors: try to touch my head.")
                .build()

        say.run()

        // Get the Touch service from the QiContext.
        val touch = qiContext.touch

        // Get the head touch sensor.
        val localHeadTouchSensor = touch.getSensor("Head/Touch")
        // Add onStateChanged listener.
        localHeadTouchSensor.addOnStateChangedListener { touchState ->
            val message = "Sensor " + (if (touchState.touched) "touched" else "released") + " at " + touchState.time
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
        }
        headTouchSensor = localHeadTouchSensor
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove onStateChanged listeners.
        headTouchSensor?.removeAllOnStateChangedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

}
