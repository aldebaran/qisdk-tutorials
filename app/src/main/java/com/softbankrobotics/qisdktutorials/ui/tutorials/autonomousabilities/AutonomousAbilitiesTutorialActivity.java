package com.softbankrobotics.qisdktutorials.ui.tutorials.autonomousabilities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the autonomous abilities tutorial.
 */
public class AutonomousAbilitiesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private ConversationView conversationView;
    // The button used to toggle the abilities.
    private Button button;
    // A boolean used to store the abilities status.
    private boolean abilitiesHeld = false;
    // The holder for the abilities.
    private Holder holder;
    // The QiContext provided by the QiSDK.
    private QiContext qiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);
        // Find the button in the view.
        button = findViewById(R.id.button);
        // Set the button onClick listener.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that the Activity owns the focus.
                if (qiContext != null) {
                    toggleAbilities();
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
        return R.layout.activity_autonomous_abilities_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext;

        String textToSay = "My autonomous abilities can be disabled: click on the button to hold/release them.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the QiContext.
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void toggleAbilities() {
        // Disable the button.
        button.setEnabled(false);

        if (abilitiesHeld) {
            releaseAbilities(holder);
        } else {
            holdAbilities(qiContext);
        }
    }

    private void holdAbilities(QiContext qiContext) {
        // Build and store the holder for the abilities.
        holder = HolderBuilder.with(qiContext)
                              .withAutonomousAbilities(
                                      AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                                      AutonomousAbilitiesType.BASIC_AWARENESS,
                                      AutonomousAbilitiesType.AUTONOMOUS_BLINKING
                              )
                              .build();

        // Hold the abilities asynchronously.
        Future<Void> holdFuture = holder.async().hold();

        // Chain the hold with a consumer on the UI thread.
        holdFuture.andThenConsume(Qi.onUiThread(new Consumer<Void>() {
            @Override
            public void consume(Void ignore) throws Throwable {
                displayLine("Abilities held.", ConversationItemType.INFO_LOG);
                // Store the abilities status.
                abilitiesHeld = true;
                // Change the button text.
                button.setText(R.string.release);
                // Enable the button.
                button.setEnabled(true);
            }
        }));
    }

    private void releaseAbilities(Holder holder) {
        // Release the holder asynchronously.
        Future<Void> releaseFuture = holder.async().release();

        // Chain the release with a consumer on the UI thread.
        releaseFuture.andThenConsume(Qi.onUiThread(new Consumer<Void>() {
            @Override
            public void consume(Void ignore) throws Throwable {
                displayLine("Abilities released.", ConversationItemType.INFO_LOG);
                // Store the abilities status.
                abilitiesHeld = false;
                // Change the button text.
                button.setText(getString(R.string.hold));
                // Enable the button.
                button.setEnabled(true);
            }
        }));
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
