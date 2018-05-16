package com.softbankrobotics.qisdktutorials.ui.tutorials.enforcetabletreachability;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
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

    // Store action button
    private Button enforceTabletReachabilityButton;

    // Store QiContext
    private QiContext qiContext;

    // Store action future
    private Future<Void> enforceTabletReachabilityFuture;

    // Runnable object used to start action asynchronously when the button is clicked
    private Runnable startEnforceTabletReachability = new Runnable() {
        @Override
        public void run() {
            // Get actuation service
            Actuation actuation = qiContext.getActuation();

            // Build EnforceTabletReachability action
            EnforceTabletReachability enforceTabletReachability = actuation.makeEnforceTabletReachability(qiContext.getRobotContext());

            // Update button text when the action starts
            enforceTabletReachability.setOnStartedListener(new EnforceTabletReachability.OnStartedListener() {
                   @Override
                   public void onStarted() {
                       setButtonText("Stop action");
                   }
               }
            );

            // Run the action asynchronously
            enforceTabletReachabilityFuture = enforceTabletReachability.async().run();

            // Update button text when the action fishishes and log eventual errors
            enforceTabletReachabilityFuture.thenConsume(new Consumer<Future<Void>>() {
                @Override
                public void consume(Future<Void> voidFuture) throws Throwable {
                    setButtonText("Enforce Tablet Reachability");
                    if (voidFuture.hasError()) {
                        Log.e(TAG, voidFuture.getErrorMessage());
                    }
                }
            });
        }
    };

    private void setButtonText(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enforceTabletReachabilityButton.setText(str);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        enforceTabletReachabilityButton = findViewById(R.id.tablet_reachability_button);
        enforceTabletReachabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enforceTabletReachabilityFuture == null || enforceTabletReachabilityFuture.isDone()) {
                    AsyncTask.execute(startEnforceTabletReachability);
                } else {
                    enforceTabletReachabilityFuture.requestCancellation();
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
        return R.layout.activity_enforce_tablet_reachability_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;

        // Introduction Say
        String textToSay = "I can enforce my tablet reachability by limiting my movements. Try it out while I count!";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        startCounting();
    }

    private void startCounting() {
        int sayCounter = 0;

        while (qiContext != null) {
            // Say the next number
            String numberToSay = String.valueOf(++sayCounter);
            displayLine(numberToSay, ConversationItemType.ROBOT_OUTPUT);

            Say say = SayBuilder.with(qiContext)
                    .withText(numberToSay)
                    .build();

            say.run();
        }
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
