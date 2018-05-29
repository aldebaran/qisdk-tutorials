package com.softbankrobotics.qisdktutorials.ui.tutorials.enforcetabletreachability;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.EnforceTabletReachabilityBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.EnforceTabletReachability;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the EnforceTabletReachability tutorial.
 */
public class EnforceTabletReachabilityTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "TabletReachActivity";
    private ConversationView conversationView;

    // Store QiContext
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
        enforceTabletReachabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enforceTabletReachability == null) {
                    String errorLog = "EnforceTabletReachability is null";
                    displayLine(errorLog, ConversationItemType.ERROR_LOG);
                    Log.i(TAG, errorLog);
                } else if (enforceTabletReachabilityFuture == null || enforceTabletReachabilityFuture.isDone()) {
                    enforceTabletReachabilityFuture = startEnforceTabletReachability();
                } else {
                    enforceTabletReachabilityFuture.requestCancellation();
                }
            }
        });

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    private Future<Void> startEnforceTabletReachability() {
        // On started listener
        Future<Void> startListenerFuture = enforceTabletReachability.async().addOnStartedListener(new EnforceTabletReachability.OnStartedListener() {
               @Override
               public void onStarted() {
                   // Display log
                   String infoLog = "The EnforceTabletReachability action has started.";
                   displayLine(infoLog, ConversationItemType.INFO_LOG);
                   Log.i(TAG, infoLog);
               }
           }
        );

        // On position reached listener
        Future<Void> positionReachedListenerFuture = enforceTabletReachability.async().addOnPositionReachedListener(new EnforceTabletReachability.OnPositionReachedListener() {
            @Override
            public void onPositionReached() {
                // Display log
                String infoLog = "The tablet now is in position.";
                displayLine(infoLog, ConversationItemType.INFO_LOG);
                Log.i(TAG, infoLog);

                // Update button text
                setButtonText(getResources().getString(R.string.cancel_action));

                // Say action
                String textToSay = "My movements are now limited. Cancel the action to see the difference.";
                displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

                Say say = SayBuilder.with(qiContext)
                        .withText(textToSay)
                        .build();

                say.run();
            }
        });

        // Run the action after adding the listeners asynchronously
        Future<Void> addListenerFuture = Future.waitAll(startListenerFuture, positionReachedListenerFuture);

        Future<Void> enforceTabletReachabilityFuture = addListenerFuture.thenCompose(new Function<Future<Void>, Future<Void>>() {
            @Override
            public Future<Void> execute(Future<Void> voidFuture) throws Throwable {
                return enforceTabletReachability.async().run();
            }
        });

        // Handle the action's end
        enforceTabletReachabilityFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                // Remove all listeners
                enforceTabletReachability.removeAllOnStartedListeners();
                enforceTabletReachability.removeAllOnPositionReachedListeners();

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
                String textToSay = "My movements are back to normal. Run the action again to see the difference.";
                displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

                Say say = SayBuilder.with(qiContext)
                        .withText(textToSay)
                        .build();

                say.run();
            }
        });

        return enforceTabletReachabilityFuture;
    }

    private void setButtonText(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enforceTabletReachabilityButton.setText(str);
            }
        });
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

        // Build introduction Say
        String textToSay = "I can enforce my tablet reachability by limiting my movements. Try it out!";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Build EnforceTabletReachability action.
        enforceTabletReachability = EnforceTabletReachabilityBuilder.with(qiContext).build();
        enableButton();
    }

    private void enableButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enforceTabletReachabilityButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
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
