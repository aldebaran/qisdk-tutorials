/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.bookmarks

import android.media.MediaPlayer
import android.os.Bundle
import android.support.annotation.RawRes
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
import com.aldebaran.qi.sdk.`object`.conversation.Bookmark
import com.aldebaran.qi.sdk.`object`.conversation.BookmarkStatus
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.conversation_layout.*

private const val TAG = "BookmarksActivity"

/**
 * The activity for the Bookmarks tutorial.
 */
class BookmarksTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var conversationBinder: ConversationBinder
    private var mediaPlayer: MediaPlayer? = null

    // Store the QiChatbot.
    private lateinit var qiChatbot: QiChatbot
    // Store the Chat action.
    private lateinit var chat: Chat
    // Store the proposal bookmark.
    private var proposalBookmark: Bookmark? = null
    // Store the dog BookmarkStatus.
    private lateinit var dogBookmarkStatus: BookmarkStatus
    // Store the elephant BookmarkStatus.
    private lateinit var elephantBookmarkStatus: BookmarkStatus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onStop() {
        mediaPlayer?.let {
            it.release()
            mediaPlayer = null
        }
        super.onStop()
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

        // Create a topic.
        val topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.mimic_animal)
                .build()

        val qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build()
                .also { this.qiChatbot = it }

        // Create a new Chat action.
        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build()
                .also { this.chat = it }

        // Get the bookmarks from the topic.
        val bookmarks = topic.bookmarks
        // Get the proposal bookmark.
        proposalBookmark = bookmarks["mimic_proposal"]

        // Go to the proposal bookmark when the Chat action starts.
        chat.addOnStartedListener(this::sayProposal)

        // Get the mimic bookmarks.
        val dogBookmark = bookmarks["dog_mimic"]
        val elephantBookmark = bookmarks["elephant_mimic"]

        // Create a BookmarkStatus for each bookmark.
        val dogBookmarkStatus = qiChatbot.bookmarkStatus(dogBookmark)
        val elephantBookmarkStatus = qiChatbot.bookmarkStatus(elephantBookmark)

        // Mimic a dog when the dog mimic bookmark is reached.
        dogBookmarkStatus.addOnReachedListener { mimicDog(qiContext) }
        this.dogBookmarkStatus = dogBookmarkStatus

        // Mimic an elephant when the elephant mimic bookmark is reached.
        elephantBookmarkStatus.addOnReachedListener { mimicElephant(qiContext) }
        this.elephantBookmarkStatus = elephantBookmarkStatus

        // Run the Chat action asynchronously.
        chat.async().run()
        this.chat = chat
    }

    override fun onRobotFocusLost() {
        conversationBinder.unbind()

        // Remove the listeners from the Chat action.
        chat.removeAllOnStartedListeners()

        // Remove the listeners on each BookmarkStatus.
        dogBookmarkStatus.removeAllOnReachedListeners()
        elephantBookmarkStatus.removeAllOnReachedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun mimicDog(qiContext: QiContext) {
        val message = "Dog mimic."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)
        mimic(R.raw.dog_a001, R.raw.dog_sound, qiContext)
    }

    private fun mimicElephant(qiContext: QiContext) {
        val message = "Elephant mimic."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)
        mimic(R.raw.elephant_a001, R.raw.elephant_sound, qiContext)
    }

    private fun mimic(@RawRes mimicResource: Int, @RawRes soundResource: Int, qiContext: QiContext) {
        // Create an animation from the mimic resource.
        val animation = AnimationBuilder.with(qiContext)
                .withResources(mimicResource)
                .build()

        // Create an animate action.
        val animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build()

        animate.addOnStartedListener {
            mediaPlayer?.let {
                it.release()
                mediaPlayer = null
            }

            mediaPlayer = MediaPlayer.create(this, soundResource). apply { start() }
            mediaPlayer?.start()
        }

        // Run the animate action asynchronously.
        animate.async().run().andThenConsume { sayProposal() }
    }

    private fun sayProposal() {
        qiChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }

}
