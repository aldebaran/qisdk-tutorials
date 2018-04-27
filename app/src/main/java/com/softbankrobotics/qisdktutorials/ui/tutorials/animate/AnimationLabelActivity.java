package com.softbankrobotics.qisdktutorials.ui.tutorials.animate;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

public class AnimationLabelActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "AnimationLabelActivity";

    private ConversationView conversationView;

    // Store the Animate action.
    private Animate animate;

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
        String textToSay = "I can trigger events using animation labels.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.dance_b001) // Set the animation resource.
                .build(); // Build the animation.

        // Create an animate action.
        animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build(); // Build the animate action.

        animate.setOnLabelReachedListener(new Animate.OnLabelReachedListener() {
            @Override
            public void onLabelReached(String label, Long time) {
                Say sayLabel = SayBuilder.with(qiContext)
                        .withText(label)
                        .build();

                sayLabel.async().run();

                displayLine(label, ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Set an on started listener to the animate action.
        animate.setOnStartedListener(new Animate.OnStartedListener() {
            @Override
            public void onStarted() {
                String message = "Animation started.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });

        // Run the animate action asynchronously.
        Future<Void> animateFuture = animate.async().run();

        // Add a consumer to the action execution.
        animateFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    String message = "Animation finished with success.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);
                } else if (future.hasError()) {
                    String message = "Animation finished with error.";
                    Log.e(TAG, message, future.getError());
                    displayLine(message, ConversationItemType.ERROR_LOG);
                }
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the signal listeners from the animate action.
        if (animate != null) {
            animate.setOnStartedListener(null);
            animate.setOnLabelReachedListener(null);
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