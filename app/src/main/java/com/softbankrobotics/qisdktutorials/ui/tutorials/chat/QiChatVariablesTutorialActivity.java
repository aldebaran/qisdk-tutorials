package com.softbankrobotics.qisdktutorials.ui.tutorials.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

import java.util.Collections;

/**
 * The activity for the QiChatVariables tutorial.
 */
public class QiChatVariablesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private EditText variableEditText;
    private ConversationView conversationView;

    // Store the variable.
    private QiChatVariable variable;
    // Store the QiChatbot.
    private QiChatbot qiChatbot;
    // Store the Chat action.
    private Chat chat;
    // Store the Bookmark used to read the variable.
    private Bookmark readBookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        variableEditText = findViewById(R.id.variable_editText);
        variableEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    retrieveAndAssignVariable();
                }
                return false;
            }
        });

        // Assign variable on assign button clicked.
        ImageButton assignButton = findViewById(R.id.assign_button);
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveAndAssignVariable();
            }
        });

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qi_chat_variables_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "Assign a value to the variable.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.variable)
                .build();

        readBookmark = topic.getBookmarks().get("read");

        // Create a new QiChatbot.
        qiChatbot = qiContext.getConversation()
                .makeQiChatbot(qiContext.getRobotContext(), Collections.singletonList(topic));

        // Create a new Chat action.
        chat = qiContext.getConversation()
                .makeChat(qiContext.getRobotContext(), Collections.<Chatbot>singletonList(qiChatbot));


        // Get the variable.
        variable = qiChatbot.variable("var");

        chat.addOnHeardListener(new Chat.OnHeardListener() {
            @Override
            public void onHeard(Phrase heardPhrase) {
                displayLine(heardPhrase.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });

        chat.addOnSayingChangedListener(new Chat.OnSayingChangedListener() {
            @Override
            public void onSayingChanged(Phrase sayingPhrase) {
                String text = sayingPhrase.getText();
                if (!TextUtils.isEmpty(text)) {
                    displayLine(text, ConversationItemType.ROBOT_OUTPUT);
                }
            }
        });

        // Run the Chat action asynchronously.
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (chat != null) {
            chat.removeAllOnHeardListeners();
            chat.removeAllOnSayingChangedListeners();
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
        variable.async().setValue(value).andThenConsume(new Consumer<Void>() {
            @Override
            public void consume(Void ignore) throws Throwable {
                // Read the value.
                qiChatbot.async().goToBookmark(readBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            }
        });
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationView.addLine(text, type);
            }
        });
    }
}
