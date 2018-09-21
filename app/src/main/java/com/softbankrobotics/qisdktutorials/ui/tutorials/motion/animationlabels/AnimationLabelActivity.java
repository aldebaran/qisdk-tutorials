/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animationlabels;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

public class AnimationLabelActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "AnimationLabelActivity";

    private ConversationView conversationView;
    private ConversationBinder conversationBinder;

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
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_layout;
    }

    @Override
    public void onRobotFocusGained(final QiContext qiContext) {
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        Say say = SayBuilder.with(qiContext)
                .withText("I can trigger events using animation labels: I will synchronize my speech with my dance.")
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

        // Say and display the name of the reached labels
        animate.addOnLabelReachedListener((label, time) -> {
            Say sayLabel = SayBuilder.with(qiContext)
                    .withText(label)
                    .build();

            sayLabel.async().run();
        });

        // Add an on started listener to the animate action.
        animate.addOnStartedListener(() -> {
            String message = "Animation started.";
            Log.i(TAG, message);
            displayLine(message, ConversationItemType.INFO_LOG);
        });

        // Run the animate action asynchronously.
        Future<Void> animateFuture = animate.async().run();

        // Add a lambda to the action execution.
        animateFuture.thenConsume(future -> {
            if (future.isSuccess()) {
                String message = "Animation finished with success.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            } else if (future.hasError()) {
                String message = "Animation finished with error.";
                Log.e(TAG, message, future.getError());
                displayLine(message, ConversationItemType.ERROR_LOG);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        if (conversationBinder != null) {
            conversationBinder.unbind();
        }

        // Remove the signal listeners from the animate action.
        if (animate != null) {
            animate.removeAllOnStartedListeners();
            animate.removeAllOnLabelReachedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(() -> conversationView.addLine(text, type));
    }
}