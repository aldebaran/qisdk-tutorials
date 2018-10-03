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

    private ConversationStatus.OnSayingChangedListener onSayingChangedListener;
    private ConversationStatus.OnHeardListener onHeardListener;

    private ConversationBinder(@NonNull final ConversationStatus conversationStatus) {
        this.conversationStatus = conversationStatus;
    }

    private void bind(@NonNull ConversationView conversationView) {
        onSayingChangedListener = sayingPhrase -> {
            String text = sayingPhrase.getText();
            if (!TextUtils.isEmpty(text)) {
                conversationView.post(() -> conversationView.addLine(text, ConversationItemType.ROBOT_OUTPUT));
            }
        };
        onHeardListener = heardPhrase -> {
            String text = heardPhrase.getText();
            conversationView.post(() -> conversationView.addLine(text, ConversationItemType.HUMAN_INPUT));
        };

        conversationStatus.addOnSayingChangedListener(onSayingChangedListener);
        conversationStatus.addOnHeardListener(onHeardListener);
    }

    public void unbind() {
        conversationStatus.removeOnSayingChangedListener(onSayingChangedListener);
        conversationStatus.removeOnHeardListener(onHeardListener);
        onSayingChangedListener = null;
        onHeardListener = null;
    }

    @NonNull
    static ConversationBinder binding(@NonNull ConversationStatus conversationStatus, @NonNull ConversationView conversationView) {
        ConversationBinder conversationBinder = new ConversationBinder(conversationStatus);
        conversationBinder.bind(conversationView);
        return conversationBinder;
    }
}
