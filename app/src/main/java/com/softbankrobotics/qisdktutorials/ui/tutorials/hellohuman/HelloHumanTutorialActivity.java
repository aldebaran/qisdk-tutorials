package com.softbankrobotics.qisdktutorials.ui.tutorials.hellohuman;

import android.os.Bundle;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the Hello human tutorial.
 */
public class HelloHumanTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private ConversationView conversationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

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
        return R.layout.conversation_layout;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        displayHelloHuman();

        // Create a new say action.
        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                            .withText("Hello human!") // Set the text to say.
                            .build(); // Build the say action.

        // Execute the action.
        say.run();
    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void displayHelloHuman() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationView.addLine("Hello human!", ConversationItemType.ROBOT_OUTPUT);
            }
        });
    }
}
