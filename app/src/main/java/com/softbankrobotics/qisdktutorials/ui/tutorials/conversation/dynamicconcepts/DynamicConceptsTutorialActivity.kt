/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.sdk.Qi

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.`object`.conversation.EditablePhraseSet
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils
import kotlinx.android.synthetic.main.activity_dynamic_concepts_tutorial.*

/**
 * The activity for the Dynamic concepts tutorial.
 */
class DynamicConceptsTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var greetingAdapter: GreetingAdapter
    private lateinit var conversationBinder: ConversationBinder

    // Store the greetings dynamic concept.
    private lateinit var greetings: EditablePhraseSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleAddClick()
            }
            false
        }

        // Create adapter for recycler view.
        // Remove greeting.
        val greetingRemovedListener = object : OnGreetingRemovedListener {
            override fun onGreetingRemoved(greeting: String) {
                this@DynamicConceptsTutorialActivity.removeGreeting(greeting)
            }
        }

        greetingAdapter = GreetingAdapter(greetingRemovedListener)

        // Setup recycler view.
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = greetingAdapter

        // Add greeting on add button clicked.
        add_button.setOnClickListener { handleAddClick() }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_dynamic_concepts_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("Add more greetings to my dynamic concept and say \"Hello\".")
                .build()

        say.run()

        // Create a topic.
        val topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.greetings_dynamic)
                .build()

        // Create a new QiChatbot.
        val qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build()

        // Create a new Chat action.
        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build()

        // Get the greetings dynamic concept.
        greetings = qiChatbot.dynamicConcept("greetings")

        // Add default content to the dynamic concept.
        addGreeting("Hello")
        addGreeting("Hi")

        // Run the Chat action asynchronously.
        chat.async().run()
    }

    override fun onRobotFocusLost() {
        conversationBinder.unbind()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun handleAddClick() {
        val greeting = editText.text.toString()
        editText.setText("")
        KeyboardUtils.hideKeyboard(this)
        // Add greeting only if new.
        if (greeting.isNotEmpty() && !greetingAdapter.containsGreeting(greeting)) {
            addGreeting(greeting)
        }
    }

    private fun addGreeting(greeting: String) {
        greetings.async().addPhrases(listOf(Phrase(greeting))).andThenConsume(Qi.onUiThread(Consumer {
                        greetingAdapter.addGreeting(greeting)
        }))
    }

    private fun removeGreeting(greeting: String) {
        greetings.async().removePhrases(listOf(Phrase(greeting))).andThenConsume(Qi.onUiThread(Consumer {
                        greetingAdapter.removeGreeting(greeting)
        }))
    }
}
