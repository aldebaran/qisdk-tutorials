/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import android.support.annotation.StringRes
import android.util.Log

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.`object`.conversation.QiChatVariable
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.conversation.TopicStatus
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel

private const val TAG = "CategoriesRobot"

private const val LEVEL_BASIC = "basic"
private const val LEVEL_ADVANCED = "advanced"

/**
 * The robot for the tutorial categories.
 */
internal class CategoriesRobot(private val presenter: CategoriesContract.Presenter) : CategoriesContract.Robot, RobotLifecycleCallbacks {
    private var talkTopicStatus: TopicStatus? = null
    private var moveTopicStatus: TopicStatus? = null
    private var smartTopicStatus: TopicStatus? = null
    private var qiChatbot: QiChatbot? = null
    private var chatFuture: Future<Void>? = null
    private var selectedCategory = TutorialCategory.TALK
    private var selectedLevel = TutorialLevel.BASIC
    private var levelVariable: QiChatVariable? = null
    private var isFirstIntro = true

    override fun register(activity: CategoriesActivity) {
        QiSDK.register(activity, this)
    }

    override fun unregister(activity: CategoriesActivity) {
        QiSDK.unregister(activity, this)
    }

    override fun stopDiscussion(tutorial: Tutorial) {
        val chatFuture = chatFuture
        if (chatFuture != null) {
            chatFuture.thenConsume {
                if (it.isCancelled) {
                    presenter.goToTutorial(tutorial)
                }
            }
            chatFuture.requestCancellation()
        } else {
            presenter.goToTutorial(tutorial)
        }
        this.chatFuture = chatFuture
    }

    override fun selectTopic(category: TutorialCategory) {
        selectedCategory = category

        val topicsAreReady = talkTopicStatus != null && moveTopicStatus != null && smartTopicStatus != null
        if (topicsAreReady) {
            enableTopic(category)
        }
    }

    override fun selectLevel(level: TutorialLevel) {
        selectedLevel = level

        if (levelVariable != null) {
            enableLevel(level)
        }
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        SayBuilder.with(qiContext)
                .withText(qiContext.getString(introSentenceRes()))
                .build()
                .run()

        isFirstIntro = false

        val commonTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.common)
                .build()

        val talkTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.talk_tutorials)
                .build()

        val moveTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.move_tutorials)
                .build()

        val smartTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.smart_tutorials)
                .build()

        val qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopics(listOf(commonTopic, talkTopic, moveTopic, smartTopic))
                .build()
                .also { this.qiChatbot = it }

        val chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build()

        talkTopicStatus = qiChatbot.topicStatus(talkTopic)
        moveTopicStatus = qiChatbot.topicStatus(moveTopic)
        smartTopicStatus = qiChatbot.topicStatus(smartTopic)

        levelVariable = qiChatbot.variable("level")

        enableLevel(selectedLevel)
        enableTopic(selectedCategory)

        qiChatbot.addOnBookmarkReachedListener { bookmark ->
            when (bookmark.name) {
                "talk" -> {
                    presenter.loadTutorials(TutorialCategory.TALK)
                    selectTopic(TutorialCategory.TALK)
                }
                "move" -> {
                    presenter.loadTutorials(TutorialCategory.MOVE)
                    selectTopic(TutorialCategory.MOVE)
                }
                "smart" -> {
                    presenter.loadTutorials(TutorialCategory.SMART)
                    selectTopic(TutorialCategory.SMART)
                }
                "basic" -> {
                    presenter.loadTutorials(TutorialLevel.BASIC)
                    selectLevel(TutorialLevel.BASIC)
                }
                "advanced" -> {
                    presenter.loadTutorials(TutorialLevel.ADVANCED)
                    selectLevel(TutorialLevel.ADVANCED)
                }
            }
        }

        qiChatbot.addOnEndedListener { presenter.goToTutorialForQiChatbotId(it) }
        this.qiChatbot = qiChatbot
        chatFuture = chat.async().run()
    }

    override fun onRobotFocusLost() {
        qiChatbot?.let {
            it.removeAllOnBookmarkReachedListeners()
            it.removeAllOnEndedListeners()
            qiChatbot = null
        }
        chatFuture = null
        talkTopicStatus = null
        moveTopicStatus = null
        smartTopicStatus = null
    }

    override fun onRobotFocusRefused(reason: String) {
        Log.i(TAG, "onRobotFocusRefused: $reason")
    }

    /**
     * Enable the topic corresponding to the specified tutorial category.
     * @param category the tutorial category
     */
    private fun enableTopic(category: TutorialCategory) {
        val talkFuture = talkTopicStatus?.async()?.setEnabled(false)
        val moveFuture = moveTopicStatus?.async()?.setEnabled(false)
        val smartFuture = smartTopicStatus?.async()?.setEnabled(false)

        Future.waitAll(talkFuture, moveFuture, smartFuture)
                .andThenConsume {
                    when (category) {
                        TutorialCategory.TALK -> talkTopicStatus?.enabled = true
                        TutorialCategory.MOVE -> moveTopicStatus?.enabled = true
                        TutorialCategory.SMART -> smartTopicStatus?.enabled = true
                    }
                }
    }

    /**
     * Enable the specified level.
     * @param level the tutorial level
     */
    private fun enableLevel(level: TutorialLevel) {
        val value = levelValueFromLevel(level)
        levelVariable?.async()?.setValue(value)
    }

    /**
     * Provides the level variable value from the specified tutorial level.
     * @param level the tutorial level
     * @return The level variable value.
     */
    private fun levelValueFromLevel(level: TutorialLevel): String {
        return when (level) {
            TutorialLevel.BASIC -> LEVEL_BASIC
            TutorialLevel.ADVANCED -> LEVEL_ADVANCED
        }
    }

    @StringRes
    private fun introSentenceRes(): Int {
        return if (isFirstIntro) R.string.categories_intro_sentence else R.string.categories_intro_sentence_variant
    }

}
