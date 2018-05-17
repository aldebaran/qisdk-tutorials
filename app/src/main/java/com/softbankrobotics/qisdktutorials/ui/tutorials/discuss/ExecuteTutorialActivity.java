package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.Conversation;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The activity for the Execute tutorial.
 */
public class ExecuteTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "ExecuteTutorialActivity";
    Conversation conversationService;
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
    public void onRobotFocusGained(QiContext qiContext) {
        // Get the conversation service.
        conversationService = qiContext.getConversation();
        // Create a topic.
        final Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.execute)
                .build();


        // Create a list of topics to pass it to qiChatbot
        List<Topic> topics = new ArrayList<>();
        topics.add(topic);

        // Create qiChatbot
        final QiChatbot qiChatbot = conversationService.makeQiChatbot(qiContext.getRobotContext(), topics);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        //Map the executor name from the topic to our qiChatbotExecutor
        executors.put("myExecutor", new MyQiChatExecutor(qiContext));
        //Set the executors to the qiChatbot
        qiChatbot.setExecutors(executors);
        List<Chatbot> chatbots = new ArrayList<>();
        chatbots.add(qiChatbot);
        //make chat with the chatbots
        Chat chat = conversationService.makeChat(qiContext.getRobotContext(), chatbots);
        chat.addOnStartedListener(new Chat.OnStartedListener() {
            @Override
            public void onStarted() {
                //Say proposal to user
                Bookmark bookmark = topic.getBookmarks().get("execute_proposal");
                qiChatbot.goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            }
        });

        chat.addOnSayingChangedListener(new Chat.OnSayingChangedListener() {
            @Override
            public void onSayingChanged(Phrase sayingPhrase) {
                //Show on screen what paper is saying
                if (!TextUtils.isEmpty(sayingPhrase.getText())) {
                    displayLine(sayingPhrase.getText(), ConversationItemType.ROBOT_OUTPUT);
                }
            }
        });
        chat.addOnHeardListener(new Chat.OnHeardListener() {
            @Override
            public void onHeard(Phrase heardPhrase) {
                //Show on screen what paper hear
                displayLine(heardPhrase.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });
        chat.async().run();

    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_layout;
    }

    /*
     *class that implement BaseQiChatExecutor
     */

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationView.addLine(text, type);
            }
        });
    }

    class MyQiChatExecutor extends BaseQiChatExecutor {
        private final QiContext qiContext;

        MyQiChatExecutor(QiContext context) {
            super(context);
            this.qiContext = context;
        }

        @Override
        public void runWith(List<String> params) {
            // This is called when execute is reached in the topic
            animate(qiContext);
        }

        @Override
        public void stop() {
            // This is called when chat is canceled or stopped.
            Log.i(TAG, "execute stopped");
        }


        private void animate(QiContext qiContext) {
            // Create an animation.
            Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                    .withResources(R.raw.raise_both_hands_b001) // Set the animation resource.
                    .build(); // Build the animation.

            // Create an animate action.
            Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                    .withAnimation(animation) // Set the animation.
                    .build(); // Build the animate action.
            displayLine("Animation started.", ConversationItemType.INFO_LOG);
            animate.run();
            displayLine("Animation finished.", ConversationItemType.INFO_LOG);

        }
    }
}
