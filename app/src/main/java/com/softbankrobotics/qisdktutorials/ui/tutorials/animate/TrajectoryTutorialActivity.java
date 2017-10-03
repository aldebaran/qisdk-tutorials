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

/**
 * The activity for the Animate tutorial (trajectory).
 */
public class TrajectoryTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "TrajectoryActivity";

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
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "I can perform trajectories: I will move forward.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.dance) // Set the animation resource.
                .build(); // Build the animation.

        // Create an animate action.
        animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build(); // Build the animate action.

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
        // Remove the on started listener from the animate action.
        if (animate != null) {
            animate.setOnStartedListener(null);
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
