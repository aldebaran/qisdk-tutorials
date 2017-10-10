package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.util.Log;

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
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.aldebaran.qi.sdk.object.sharedtopics.SharedTopics;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.List;

/**
 * The activity for the Collaborative Dialog tutorial.
 */
public class CollaborativeDialogTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "CollaborativeActivity";

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
        String textToSay = "I can use collaborative topics.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Get the SharedTopics service from the QiContext.
        SharedTopics sharedTopics = qiContext.getSharedTopics();
        // Get the English shared topics.
        List<Topic> topics = sharedTopics.topicsWithLocale(new Locale(Language.ENGLISH, Region.UNITED_STATES));

        // Create the local topic.
        Topic localTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.greetings)
                .build();

        // Create a Discuss containing the local topic and the shared topics.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(localTopic)
                .withTopics(topics)
                .build();

        // Display recommendations when the Discuss action starts.
        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                List<Phrase> recommendations = discuss.globalRecommendations().getPhrases();
                for (Phrase phrase : recommendations) {
                    String message = "Recommendation: " + phrase.getText();
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);
                }
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
        discuss.async().run();
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
