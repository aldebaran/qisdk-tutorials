/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatvariables

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionImportance
import com.aldebaran.qi.sdk.`object`.conversation.AutonomousReactionValidity
import com.aldebaran.qi.sdk.`object`.conversation.Bookmark
import com.aldebaran.qi.sdk.`object`.conversation.QiChatVariable
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils

/**
 * The activity for the QiChatVariables tutorial.
 */
class QiChatVariablesTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var variableEditText: EditText
    private lateinit var conversationView: ConversationView
    private var conversationBinder: ConversationBinder? = null

    // Store the variable.
    private var variable: QiChatVariable? = null
    // Store the QiChatbot.
    private var qiChatbot: QiChatbot? = null
    // Store the Bookmark used to read the variable.
    private var readBookmark: Bookmark? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationView = findViewById(R.id.conversationView)

        variableEditText = findViewById(R.id.variable_editText)
        variableEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                retrieveAndAssignVariable()
            }
            false
        }

        // Assign variable on assign button clicked.
        val assignButton = findViewById<ImageButton>(R.id.assign_button)
        assignButton.setOnClickListener { v -> retrieveAndAssignVariable() }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun getLayoutId(): Int = R.layout.activity_qi_chat_variables_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("Assign a value to the variable.")
                .build()

        say.run()

        // Create a topic.
        val topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.variable)
                .build()

        readBookmark = topic.bookmarks["read"]

        // Create a new QiChatbot.
        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build()

        // Create a new Chat action.
        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build()

        // Get the variable.
        variable = qiChatbot?.variable("var")

        // Run the Chat action asynchronously.
        chat.async().run()
    }

    override fun onRobotFocusLost() {
            conversationBinder?.unbind()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun retrieveAndAssignVariable() {
        val value = variableEditText.text.toString()
        variableEditText.setText("")
        KeyboardUtils.hideKeyboard(this@QiChatVariablesTutorialActivity)
        assignVariable(value)
    }

    private fun assignVariable(value: String) {
        // Set the value.
        variable?.async()?.setValue(value)?.andThenConsume { ignore ->
            // Read the value.
            qiChatbot!!.async().goToBookmark(readBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
        }
    }
}
