package com.softbankrobotics.qisdktutorials.ui.tutorials.chat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;

import com.aldebaran.qi.Consumer;
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
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.Collections;
import java.util.Map;

/**
 * The activity for the Bookmarks tutorial.
 */
public class BookmarksTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "BookmarksActivity";

    private ConversationView conversationView;
    private MediaPlayer mediaPlayer;

    // Store the QiChatbot.
    private QiChatbot qiChatbot;
    // Store the Chat action.
    private Chat chat;
    // Store the proposal bookmark.
    private Bookmark proposalBookmark;
    // Store the dog BookmarkStatus.
    private BookmarkStatus dogBookmarkStatus;
    // Store the elephant BookmarkStatus.
    private BookmarkStatus elephantBookmarkStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onStop();
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
    public void onRobotFocusGained(final QiContext qiContext) {
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.mimic_animal)
                .build();

        // Create a new QiChatbot.
        qiChatbot = qiContext.getConversation()
                .makeQiChatbot(qiContext.getRobotContext(), Collections.singletonList(topic));

        // Create a new Chat action.
        chat = qiContext.getConversation()
                .makeChat(qiContext.getRobotContext(), Collections.<Chatbot>singletonList(qiChatbot));

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        // Get the proposal bookmark.
        proposalBookmark = bookmarks.get("mimic_proposal");

        // Go to the proposal bookmark when the Chat action starts.
        chat.addOnStartedListener(new Chat.OnStartedListener() {
            @Override
            public void onStarted() {
                sayProposal();
            }
        });

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

        // Get the mimic bookmarks.
        Bookmark dogBookmark = bookmarks.get("dog_mimic");
        Bookmark elephantBookmark = bookmarks.get("elephant_mimic");

        // Create a BookmarkStatus for each bookmark.
        dogBookmarkStatus = qiChatbot.bookmarkStatus(dogBookmark);
        elephantBookmarkStatus = qiChatbot.bookmarkStatus(elephantBookmark);

        // Mimic a dog when the dog mimic bookmark is reached.
        dogBookmarkStatus.addOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                mimicDog(qiContext);
            }
        });

        // Mimic an elephant when the elephant mimic bookmark is reached.
        elephantBookmarkStatus.addOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                mimicElephant(qiContext);
            }
        });

        // Run the Chat action asynchronously.
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
            chat.removeAllOnHeardListeners();
            chat.removeAllOnSayingChangedListeners();
        }

        // Remove the listeners on each BookmarkStatus.
        if (dogBookmarkStatus != null) {
            dogBookmarkStatus.removeAllOnReachedListeners();
        }
        if (elephantBookmarkStatus != null) {
            elephantBookmarkStatus.removeAllOnReachedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void mimicDog(QiContext qiContext) {
        String message = "Dog mimic.";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);
        mimic(R.raw.dog_a001, R.raw.dog_sound, qiContext);
    }

    private void mimicElephant(QiContext qiContext) {
        String message = "Elephant mimic.";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);
        mimic(R.raw.elephant_a001, R.raw.elephant_sound, qiContext);
    }

    private void mimic(@RawRes Integer mimicResource, @RawRes final Integer soundResource, QiContext qiContext) {
        // Create an animation from the mimic resource.
        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(mimicResource)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        animate.addOnStartedListener(new Animate.OnStartedListener() {
            @Override
            public void onStarted() {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                mediaPlayer = MediaPlayer.create(BookmarksTutorialActivity.this, soundResource);
                mediaPlayer.start();
            }
        });

        // Run the animate action asynchronously.
        animate.async().run().andThenConsume(new Consumer<Void>() {
            @Override
            public void consume(Void ignore) throws Throwable {
                sayProposal();
            }
        });
    }

    private void sayProposal() {
        qiChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
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
