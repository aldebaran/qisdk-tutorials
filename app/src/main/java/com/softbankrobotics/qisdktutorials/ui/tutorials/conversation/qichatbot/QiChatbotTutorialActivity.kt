/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatbot

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "QiChatbotActivity"

/**
 * The activity for the QiChatbot tutorial.
 */
class QiChatbotTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // Store the Chat action.
    private lateinit var chat: Chat

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

    override fun getLayoutId(): Int = R.layout.conversation_layout

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("Say \"Hello\" to start the discussion.")
                .build()

        say.run()

        // Create a topic.
        val topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.greetings) // Set the topic resource.
                .build() // Build the topic.

        // Create a new QiChatbot.
        val qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build()

        // Create a new Chat action.
        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build()
                .also { this.chat = it }

        // Add an on started listener to the Chat action.
        chat.addOnStartedListener {
            val message = "Discussion started."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
        }

        this.chat = chat

        // Run the Chat action asynchronously.
        val chatFuture = this.chat.async().run()

        // Add a lambda to the action execution.
        chatFuture.thenConsume { future ->
            if (future.hasError()) {
                val message = "Discussion finished with error."
                Log.e(TAG, message, future.error)
                displayLine(message, ConversationItemType.ERROR_LOG)
            }
        }
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove the listeners from the Chat action.
        chat.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }
}
