/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.listen


import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.util.PhraseSetUtil
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "ListenTutorialActivity"

/**
 * The activity for the Listen tutorial.
 */
class ListenTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var conversationBinder: ConversationBinder

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

    override val layoutId = R.layout.conversation_layout

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can listen to you: say \"Yes\" or \"No\" to try.")
                .build()

        say.run()

        // Create the PhraseSet 1.
        val phraseSetYes = PhraseSetBuilder.with(qiContext) // Create the builder using the QiContext.
                .withTexts("yes", "OK", "alright", "let's do this") // Add the phrases Pepper will listen to.
                .build() // Build the PhraseSet.

        // Create the PhraseSet 2.
        val phraseSetNo = PhraseSetBuilder.with(qiContext) // Create the builder using the QiContext.
                .withTexts("no", "Sorry", "I can't") // Add the phrases Pepper will listen to.
                .build() // Build the PhraseSet.

        // Create a new listen action.
        val listen = ListenBuilder.with(qiContext) // Create the builder with the QiContext.
                .withPhraseSets(phraseSetYes, phraseSetNo) // Set the PhraseSets to listen to.
                .build() // Build the listen action.

        // Run the listen action and get the result.
        val listenResult = listen.run()

        val humanText = listenResult.heardPhrase.text
        Log.i(TAG, "Heard phrase: $humanText")

        // Identify the matched phrase set.
        val matchedPhraseSet = listenResult.matchedPhraseSet
        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            val msg = "Heard phrase set: yes"
            Log.i(TAG, msg)
            displayLine(msg, ConversationItemType.INFO_LOG)
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {
            val msg = "Heard phrase set: no"
            Log.i(TAG, msg)
            displayLine(msg, ConversationItemType.INFO_LOG)
        }
    }

    override fun onRobotFocusLost() {
        conversationBinder.unbind()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversation_view.addLine(text, type) }
    }
}
