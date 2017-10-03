package com.softbankrobotics.qisdktutorials.ui.tutorials.lookat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the LookAt tutorial.
 */
public class LookAtTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "LookAtTutorialActivity";

    private ConversationView conversationView;

    // Store the LookAt action.
    private LookAt lookAt;

    // Store the action execution future.
    private Future<Void> lookAtFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        // Find the button in the view.
        Button cancelButton = findViewById(R.id.cancel_button);
        // Set the button onClick listener.
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lookAtFuture != null) {
                    lookAtFuture.requestCancellation();
                }
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
        return R.layout.activity_look_at_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "I can look at things: I will look at the ground.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Get the Actuation service from the QiContext.
        Actuation actuation = qiContext.getActuation();

        // Get the robot frame.
        Frame robotFrame = actuation.robotFrame();

        // Create a transform corresponding to a 1 meter forward translation.
        Transform transform = TransformBuilder.create()
                                              .from2DTranslation(1, 0);

        // Get the Mapping service from the QiContext.
        Mapping mapping = qiContext.getMapping();

        // Create a FreeFrame with the Mapping service.
        FreeFrame targetFrame = mapping.makeFreeFrame();

        // Update the target location relatively to Pepper's current location.
        targetFrame.update(robotFrame, transform, System.currentTimeMillis());

        // Create a LookAt action.
        lookAt = LookAtBuilder.with(qiContext) // Create the builder with the context.
                                     .withFrame(targetFrame.frame()) // Set the target frame.
                                     .build(); // Build the LookAt action.

        // Set the LookAt policy to look with the head only.
        lookAt.setPolicy(LookAtMovementPolicy.HEAD_ONLY);

        // Set an on started listener on the LookAt action.
        lookAt.setOnStartedListener(new LookAt.OnStartedListener() {
            @Override
            public void onStarted() {
                String message = "LookAt action started.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });

        // Run the LookAt action asynchronously.
        lookAtFuture = lookAt.async().run();

        // Add a consumer to the action execution.
        lookAtFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    String message = "LookAt action finished with success.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);
                } else if (future.isCancelled()) {
                    String message = "LookAt action was cancelled.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);
                } else {
                    String message = "LookAt action finished with error.";
                    Log.e(TAG, message, future.getError());
                    displayLine(message, ConversationItemType.ERROR_LOG);
                }
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the on started listener from the LookAt action.
        if (lookAt != null) {
            lookAt.setOnStartedListener(null);
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
