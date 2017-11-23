package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the Discuss tutorial.
 */
public class DiscussTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "DiscussTutorialActivity";

    private ConversationView conversationView;

    // Store the Discuss action.
    private Discuss discuss;

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
        String textToSay = "Say \"Hello\" to start the discussion.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                                  .withResource(R.raw.greetings) // Set the topic resource.
                                  .build(); // Build the topic.

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext) // Create the builder using the QiContext.
                                        .withTopic(topic) // Add the topic.
                                        .build(); // Build the discuss action.

        // Set an on started listener to the discuss action.
        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                String message = "Discussion started.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });

        discuss.setOnLatestInputUtteranceChangedListener(new Discuss.OnLatestInputUtteranceChangedListener() {
            @Override
            public void onLatestInputUtteranceChanged(Phrase input) {
                displayLine(input.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });

        discuss.setOnLatestOutputUtteranceChangedListener(new Discuss.OnLatestOutputUtteranceChangedListener() {
            @Override
            public void onLatestOutputUtteranceChanged(Phrase output) {
                displayLine(output.getText(), ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Run the discuss action asynchronously.
        Future<String> discussFuture = discuss.async().run();

        // Add a consumer to the action execution.
        discussFuture.thenConsume(new Consumer<Future<String>>() {
            @Override
            public void consume(Future<String> future) throws Throwable {
                if (future.hasError()) {
                    String message = "Discussion finished with error.";
                    Log.e(TAG, message, future.getError());
                    displayLine(message, ConversationItemType.ERROR_LOG);
                }
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the listeners from the discuss action.
        if (discuss != null) {
            discuss.setOnStartedListener(null);
            discuss.setOnLatestInputUtteranceChangedListener(null);
            discuss.setOnLatestOutputUtteranceChangedListener(null);
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
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
