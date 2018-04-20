package com.softbankrobotics.qisdktutorials.ui.tutorials.enforcetabletreachability;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
 * The activity for the Animate tutorial (animation).
 */
public class EnforceTabletReachabilityTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "AnimateTutorialActivity";

    private ConversationView conversationView;

    // Store the Animate action.
    private Animate animate;
    private Button animateButton;
    private Button enforceTabletReachabilityButton;
    private QiContext qiContext;
    private Future<Void> enforceTabletReachabilityFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        animateButton = findViewById(R.id.animate_button);
        animateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAsyncAnimate();
            }
        });

        enforceTabletReachabilityButton = findViewById(R.id.toggle_tablet_reachability_button);
        enforceTabletReachabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enforceTabletReachabilityFuture == null) {
                    enforceTabletReachabilityFuture = startAsyncEnforceTabletReachability();
                    enforceTabletReachabilityButton.setText("Stop EnforceTabletReachability");
                }
                else {
                    enforceTabletReachabilityFuture.requestCancellation();
                    enforceTabletReachabilityButton.setText("Start EnforceTabletReachability");
                }
            }
        });

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    private Future<Void> startAsyncEnforceTabletReachability() {

        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
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
        return R.layout.activity_enforce_tablet_reachability_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;

        // Introduction Say
        String textToSay = "I can enforce my tablet reachability by limiting my torso and arms movements. Try it out!";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.elephant_a001) // Set the animation resource.
                .build(); // Build the animation.

        // Create and store the animate action.
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

    }

    private Future<Void> startAsyncAnimate() {
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

        return animateFuture;
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
