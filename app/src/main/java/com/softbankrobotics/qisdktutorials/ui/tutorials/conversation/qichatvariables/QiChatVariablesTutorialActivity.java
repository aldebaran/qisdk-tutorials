/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatvariables;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

/**
 * The activity for the QiChatVariables tutorial.
 */
public class QiChatVariablesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private EditText variableEditText;
    private ConversationView conversationView;
    private ConversationBinder conversationBinder;

    // Store the variable.
    private QiChatVariable variable;
    // Store the QiChatbot.
    private QiChatbot qiChatbot;
    // Store the Bookmark used to read the variable.
    private Bookmark readBookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        variableEditText = findViewById(R.id.variable_editText);
        variableEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                retrieveAndAssignVariable();
            }
            return false;
        });

        // Assign variable on assign button clicked.
        ImageButton assignButton = findViewById(R.id.assign_button);
        assignButton.setOnClickListener(v -> retrieveAndAssignVariable());

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qi_chat_variables_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        Say say = SayBuilder.with(qiContext)
                .withText("Assign a value to the variable.")
                .build();

        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.variable)
                .build();

        readBookmark = topic.getBookmarks().get("read");

        // Create a new QiChatbot.
        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        Chat chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        // Get the variable.
        variable = qiChatbot.variable("var");

        // Run the Chat action asynchronously.
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (conversationBinder != null) {
            conversationBinder.unbind();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void retrieveAndAssignVariable() {
        String value = variableEditText.getText().toString();
        variableEditText.setText("");
        KeyboardUtils.hideKeyboard(QiChatVariablesTutorialActivity.this);
        assignVariable(value);
    }

    private void assignVariable(String value) {
        // Set the value.
        variable.async().setValue(value).andThenConsume(ignore -> {
            // Read the value.
            qiChatbot.async().goToBookmark(readBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        });
    }
}
