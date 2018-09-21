/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.enforcetabletreachability;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.EnforceTabletReachabilityBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.EnforceTabletReachability;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the EnforceTabletReachability tutorial.
 */
public class EnforceTabletReachabilityTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "TabletReachActivity";
    private ConversationView conversationView;
    private ConversationBinder conversationBinder;

    // Store qiContext
    private QiContext qiContext;

    // Store the action.
    private EnforceTabletReachability enforceTabletReachability;

    // Store action button
    private Button enforceTabletReachabilityButton;

    // Store action future
    private Future<Void> enforceTabletReachabilityFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        enforceTabletReachabilityButton = findViewById(R.id.tablet_reachability_button);
        enforceTabletReachabilityButton.setOnClickListener(v -> {
            if (enforceTabletReachability == null) {
                String errorLog = "EnforceTabletReachability has not been built yet";
                displayLine(errorLog, ConversationItemType.ERROR_LOG);
                Log.e(TAG, errorLog);
            } else if (enforceTabletReachabilityFuture == null || enforceTabletReachabilityFuture.isDone()) {
                // The EnforceTabletReachability action is not running
                startEnforceTabletReachability();
            } else {
                // The EnforceTabletReachability action is running
                stopEnforceTabletReachability();
            }
        });

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    private void stopEnforceTabletReachability() {
        enforceTabletReachabilityFuture.requestCancellation();
    }

    private void startEnforceTabletReachability() {
        // Run the action asynchronously
        enforceTabletReachabilityFuture = enforceTabletReachability.async().run();

        // Handle the action's end
        enforceTabletReachabilityFuture.thenConsume(future -> {
            // Display eventual errors
            if (future.hasError()) {
                String message = "The EnforceTabletReachability action finished with error.";
                Log.e(TAG, message, future.getError());
                displayLine(message, ConversationItemType.ERROR_LOG);
            } else {
                String message = "The EnforceTabletReachability action has finished.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }

            // Update button text
            setButtonText(getResources().getString(R.string.enforce_tablet_reachability));

            // Say text when the action is cancelled
            Say say = SayBuilder.with(qiContext)
                    .withText("My movements are back to normal. Run the action again to see the difference.")
                    .build();

            say.run();
        });
    }

    private void setButtonText(final String str) {
        runOnUiThread(() -> enforceTabletReachabilityButton.setText(str));
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enforce_tablet_reachability_tutorial;
    }

    @Override
    public void onRobotFocusGained(final QiContext qiContext) {
        // Store qiContext
        this.qiContext = qiContext;

        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        // Build introduction Say
        Say say = SayBuilder.with(qiContext)
                .withText("I can enforce my tablet reachability by limiting my movements. Try it out!")
                .build();

        say.run();

        // Build EnforceTabletReachability action.
        enforceTabletReachability = EnforceTabletReachabilityBuilder.with(qiContext).build();

        // On started listener
        enforceTabletReachability.addOnStartedListener(() -> {
            // Display log
            String infoLog = "The EnforceTabletReachability action has started.";
            displayLine(infoLog, ConversationItemType.INFO_LOG);
            Log.i(TAG, infoLog);
        });

        // On position reached listener
        enforceTabletReachability.addOnPositionReachedListener(() -> {
            // Display log
            String infoLog = "The tablet now is in position.";
            displayLine(infoLog, ConversationItemType.INFO_LOG);
            Log.i(TAG, infoLog);

            // Update button text
            setButtonText(getResources().getString(R.string.cancel_action));

            Say s = SayBuilder.with(qiContext)
                    .withText("My movements are now limited. Cancel the action to see the difference.")
                    .build();

            s.run();
        });

        enableButton();
    }

    private void enableButton() {
        runOnUiThread(() -> enforceTabletReachabilityButton.setEnabled(true));
    }

    @Override
    public void onRobotFocusLost() {
        if (conversationBinder != null) {
            conversationBinder.unbind();
        }

        // Remove all listeners
        enforceTabletReachability.removeAllOnStartedListeners();
        enforceTabletReachability.removeAllOnPositionReachedListeners();

        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(() -> conversationView.addLine(text, type));
    }
}
