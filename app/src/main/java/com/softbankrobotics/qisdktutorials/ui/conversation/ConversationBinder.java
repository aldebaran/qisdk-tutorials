/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.conversation;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;

public class ConversationBinder {

    @NonNull
    private final ConversationStatus conversationStatus;

    ConversationBinder(@NonNull final ConversationStatus conversationStatus) {
        this.conversationStatus = conversationStatus;
    }

    void bind(@NonNull ConversationView conversationView) {
        conversationStatus.addOnSayingChangedListener(sayingPhrase -> {
            String text = sayingPhrase.getText();
            if (!TextUtils.isEmpty(text)) {
                conversationView.post(() -> conversationView.addLine(text, ConversationItemType.ROBOT_OUTPUT));
            }
        });
        conversationStatus.addOnHeardListener(heardPhrase -> {
            String text = heardPhrase.getText();
            conversationView.post(() -> conversationView.addLine(text, ConversationItemType.HUMAN_INPUT));
        });
    }

    public void unbind() {
        conversationStatus.removeAllOnSayingChangedListeners();
        conversationStatus.removeAllOnHeardListeners();
    }
}
