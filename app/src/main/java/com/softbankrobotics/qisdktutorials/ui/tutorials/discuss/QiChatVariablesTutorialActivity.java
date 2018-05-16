package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
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
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

/**
 * The activity for the QiChatVariables tutorial.
 */
public class QiChatVariablesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private EditText variableEditText;
    private ConversationView conversationView;

    // Store the variable.
    private QiChatVariable variable;
    // Store the Discuss action.
    private Discuss discuss;
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

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the variable.
        variable = discuss.variable("var");

        discuss.addOnLatestInputUtteranceChangedListener(new Discuss.OnLatestInputUtteranceChangedListener() {
            @Override
            public void onLatestInputUtteranceChanged(Phrase input) {
                displayLine(input.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });

        discuss.addOnLatestOutputUtteranceChangedListener(new Discuss.OnLatestOutputUtteranceChangedListener() {
            @Override
            public void onLatestOutputUtteranceChanged(Phrase output) {
                displayLine(output.getText(), ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Run the discuss action asynchronously.
        discuss.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (discuss != null) {
            discuss.removeAllOnLatestInputUtteranceChangedListeners();
            discuss.removeAllOnLatestOutputUtteranceChangedListeners();
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
                discuss.async().goToBookmarkedOutputUtterance(readBookmark);
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
