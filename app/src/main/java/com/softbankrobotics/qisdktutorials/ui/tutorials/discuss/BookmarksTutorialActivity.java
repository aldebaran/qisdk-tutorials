package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.support.annotation.RawRes;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.Map;

/**
 * The activity for the Bookmarks tutorial.
 */
public class BookmarksTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "BookmarksActivity";

    private ConversationView conversationView;

    // Store the discuss action.
    private Discuss discuss;
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

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        // Get the proposal bookmark.
        proposalBookmark = bookmarks.get("mimic_proposal");

        // Go to the proposal bookmark when the discuss action starts.
        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                sayProposal();
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

        // Get the mimic bookmarks.
        Bookmark dogBookmark = bookmarks.get("dog_mimic");
        Bookmark elephantBookmark = bookmarks.get("elephant_mimic");

        // Create a BookmarkStatus for each bookmark.
        dogBookmarkStatus = discuss.bookmarkStatus(dogBookmark);
        elephantBookmarkStatus = discuss.bookmarkStatus(elephantBookmark);

        // Mimic a dog when the dog mimic bookmark is reached.
        dogBookmarkStatus.setOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                mimicDog(qiContext);
            }
        });

        // Mimic an elephant when the elephant mimic bookmark is reached.
        elephantBookmarkStatus.setOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                mimicElephant(qiContext);
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

        // Remove the listener on each BookmarkStatus.
        if (dogBookmarkStatus != null) {
            dogBookmarkStatus.setOnReachedListener(null);
        }
        if (elephantBookmarkStatus != null) {
            elephantBookmarkStatus.setOnReachedListener(null);
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
        mimic(R.raw.dog_a001, qiContext);
    }

    private void mimicElephant(QiContext qiContext) {
        String message = "Elephant mimic.";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);
        mimic(R.raw.elephant_a001, qiContext);
    }

    private void mimic(@RawRes Integer mimicResource, QiContext qiContext) {
        // Create an animation from the mimic resource.
        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(mimicResource)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        // Run the animate action asynchronously.
        animate.async().run().andThenConsume(new Consumer<Void>() {
            @Override
            public void consume(Void ignore) throws Throwable {
                sayProposal();
            }
        });
    }

    private void sayProposal() {
        discuss.goToBookmarkedOutputUtterance(proposalBookmark);
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
