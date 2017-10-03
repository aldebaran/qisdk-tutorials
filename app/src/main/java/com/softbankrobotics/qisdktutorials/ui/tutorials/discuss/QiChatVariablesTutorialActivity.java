package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

/**
 * The activity for the QiChatVariables tutorial.
 */
public class QiChatVariablesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private EditText variableEditText;

    // Store the variable.
    private QiChatVariable variable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        variableEditText = findViewById(R.id.variable_editText);
        Button validateButton = findViewById(R.id.validate_button);

        variableEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateVariable();
                }
                return false;
            }
        });

        // Save variable on validate button clicked.
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVariable();
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
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.variable)
                .build();

        // Create a new discuss action.
        Discuss discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the variable.
        variable = discuss.variable("var");

        // Run the discuss action asynchronously.
        discuss.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void updateVariable() {
        String nickname = variableEditText.getText().toString();
        variableEditText.setText("");
        KeyboardUtils.hideKeyboard(this);
        if (!nickname.isEmpty()) {
            setVariable(nickname);
        }
    }

    private void setVariable(String nickname) {
        // Set the nickname.
        variable.async().setValue(nickname);
    }
}
