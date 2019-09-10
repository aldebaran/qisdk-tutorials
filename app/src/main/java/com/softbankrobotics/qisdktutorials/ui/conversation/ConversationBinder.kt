/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.conversation

import android.text.TextUtils

import com.aldebaran.qi.sdk.`object`.conversation.ConversationStatus

class ConversationBinder private constructor(private val conversationStatus: ConversationStatus) {

    private var onSayingChangedListener: ConversationStatus.OnSayingChangedListener? = null
    private var onHeardListener: ConversationStatus.OnHeardListener? = null

    private fun bind(conversationView: ConversationView) {
        onSayingChangedListener = ConversationStatus.OnSayingChangedListener { sayingPhrase ->
            val text = sayingPhrase.text
            if (!TextUtils.isEmpty(text)) {
                conversationView.post { conversationView.addLine(text, ConversationItemType.ROBOT_OUTPUT) }
            }
        }

        onHeardListener = ConversationStatus.OnHeardListener { heardPhrase ->
            val text = heardPhrase.text
            conversationView.post { conversationView.addLine(text, ConversationItemType.HUMAN_INPUT) }
        }

        conversationStatus.addOnSayingChangedListener(onSayingChangedListener)
        conversationStatus.addOnHeardListener(onHeardListener)
    }

    fun unbind() {
        conversationStatus.removeOnSayingChangedListener(onSayingChangedListener)
        conversationStatus.removeOnHeardListener(onHeardListener)
        onSayingChangedListener = null
        onHeardListener = null
    }

    companion object {

        internal fun binding(conversationStatus: ConversationStatus, conversationView: ConversationView): ConversationBinder {
            val conversationBinder = ConversationBinder(conversationStatus)
            conversationBinder.bind(conversationView)
            return conversationBinder
        }
    }
}
