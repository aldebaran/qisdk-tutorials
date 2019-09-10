/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.execute

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionImportance
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionValidity
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatExecutor
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

import java.util.HashMap

private const val TAG = "ExecuteTutorialActivity"

/**
 * The activity for the Execute tutorial.
 */
class ExecuteTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {
    private lateinit var conversationBinder: ConversationBinder
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

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        // Create a topic.
        val topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.execute)
                .build()

        // Create a qiChatbot
        val qiChatbot = QiChatbotBuilder.with(qiContext).withTopic(topic).build()
        val executors = HashMap<String, QiChatExecutor>()

        // Map the executor name from the topic to our qiChatbotExecutor
        executors["myExecutor"] = MyQiChatExecutor(qiContext)

        // Set the executors to the qiChatbot
        qiChatbot.executors = executors

        // Build chat with the chatbots
        val chat = ChatBuilder.with(qiContext).withChatbot(qiChatbot).build().also { chat = it }
        chat.addOnStartedListener {
            //Say proposal to user
            val bookmark = topic.bookmarks["execute_proposal"]
            qiChatbot.goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
        }

        chat.async().run()
        this.chat = chat
    }

    override fun onRobotFocusLost() {
        Log.i(TAG, "Focus lost.")

        conversationBinder.unbind()

        // Remove the listeners from the chat.
        chat.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    override fun getLayoutId(): Int = R.layout.conversation_layout


    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

    internal inner class MyQiChatExecutor(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {

        override fun runWith(params: List<String>) {
            // This is called when execute is reached in the topic
            animate(qiContext)
        }

        override fun stop() {
            // This is called when chat is canceled or stopped.
            displayLine("QiChatExecutor stopped", ConversationItemType.INFO_LOG)
            Log.i(TAG, "QiChatExecutor stopped")
        }


        private fun animate(qiContext: QiContext) {
            // Create an animation.
            val animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                    .withResources(R.raw.raise_both_hands_b001) // Set the animation resource.
                    .build() // Build the animation.

            // Create an animate action.
            val animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                    .withAnimation(animation) // Set the animation.
                    .build() // Build the animate action.
            displayLine("Animation started.", ConversationItemType.INFO_LOG)
            animate.run()
            displayLine("Animation finished.", ConversationItemType.INFO_LOG)
        }
    }
}
