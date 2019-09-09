/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.chatlocale

import android.os.Bundle
import android.util.Log
import android.widget.RadioButton

import com.aldebaran.qi.Future
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
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.ConversationStatus
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.conversation.Topic
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity

    private const val TAG = "ChatLocaleActivity"

/**
 * The tutorial for choosing a locale for a Chat.
 */
class ChatLocaleTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var enButton: RadioButton? = null
    private var jaButton: RadioButton? = null
    private var conversationView: ConversationView? = null
    private var conversationBinder: ConversationBinder? = null

    // Store the Chat actions.
    private var chatEN: Chat? = null
    private var chatJA: Chat? = null
    // Store the action execution future.
    private var currentChatFuture: Future<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enButton = findViewById(R.id.enButton)
        jaButton = findViewById(R.id.jaButton)
        conversationView = findViewById(R.id.conversationView)

        // Change the locale to English when checked.
        enButton?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                disableButtons()
                switchToChat(chatEN)
            }
        }

        // Change the locale to Japanese when checked.
        jaButton?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                disableButtons()
                switchToChat(chatJA)
            }
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onResume() {
        super.onResume()

        // Disable and uncheck buttons.
        disableButtons()
        enButton?.isChecked = false
        jaButton?.isChecked = false
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun getLayoutId(): Int = R.layout.activity_chat_locale_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView?.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("Select the locale of the discussion and talk to me.")
                .build()

        say.run()

        // Prepare the Chat actions.
        buildEnglishChat(qiContext)
        buildJapaneseChat(qiContext)

        enableButtons()
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()


        // Remove the listeners from the Chat actions.
        chatEN?.removeAllOnStartedListeners()
        chatJA?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun buildEnglishChat(qiContext: QiContext) {
        val locale = Locale(Language.ENGLISH, Region.UNITED_STATES)
        chatEN = buildChat(qiContext, "hello-en.top", locale)
    }

    private fun buildJapaneseChat(qiContext: QiContext) {
        val locale = Locale(Language.JAPANESE, Region.JAPAN)
        chatJA = buildChat(qiContext, "hello-ja.top", locale)
    }

    private fun buildChat(qiContext: QiContext, topicAssetName: String, locale: Locale): Chat {
        // Create a topic from the asset file.
        val topic = TopicBuilder.with(qiContext)
                .withAsset(topicAssetName)
                .build()

        val startBookmark = topic.bookmarks["start"]

        // Create a new QiChatbot with the specified Locale.
        val qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .withLocale(locale)
                .build()

        // Create a new Chat action with the specified Locale.
        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .withLocale(locale)
                .build()

        // Enable buttons when the Chat starts.
        chat.addOnStartedListener {
            enableButtons()
            val message = "Discussion is now in " + locale.language + "."
            Log.i(TAG, message)
            displayLine(message, ConversationItemType.INFO_LOG)
            qiChatbot.async().goToBookmark(startBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
        }

        return chat
    }

    private fun switchToChat(chat: Chat?) {
        if (currentChatFuture != null) {
            // Cancel the current discussion.
            currentChatFuture!!.requestCancellation()
            // Run the Chat when the discussion stops.
            currentChatFuture!!.thenConsume { ignored -> runChat(chat) }
        } else {
            // If no current discussion, just run the Chat.
            runChat(chat)
        }
    }

    private fun runChat(chat: Chat?) {
        if (chat == null) return
        currentChatFuture = chat.async().run()
    }

    private fun disableButtons() {
        runOnUiThread {
            enButton?.isEnabled = false
            jaButton?.isEnabled = false
        }
    }

    private fun enableButtons() {
        runOnUiThread {
            enButton?.isEnabled = true
            jaButton?.isEnabled = true
        }
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView?.addLine(text, type) }
    }

}
