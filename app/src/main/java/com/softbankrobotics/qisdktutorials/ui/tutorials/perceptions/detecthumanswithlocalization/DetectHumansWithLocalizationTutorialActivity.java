package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for detecting humans with localization.
 */
public class DetectHumansWithLocalizationTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "DetectHumansWithLoc";

    private ConversationView conversationView;

    // Store the LocalizeAndMap action.
    private LocalizeAndMap localizeAndMap;

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
        return R.layout.activity_detect_humans_with_localization_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "I will start my localization. Please be sure to be at least at 3 meters away from me while I scan the place.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create a LocalizeAndMap action.
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

        // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
        localizeAndMap.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    String message = "Robot is localized.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);

                    String text = "I'm now localized. Try to come to me from the side and I will turn towards you.";
                    displayLine(text, ConversationItemType.ROBOT_OUTPUT);
                    SayBuilder.with(qiContext)
                            .withText(text)
                            .build()
                            .run();
                    break;
            }
        });

        String message = "Localizing...";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);

        // Execute the LocalizeAndMap action asynchronously.
        Future<Void> localization = localizeAndMap.async().run();

        // Add a lambda to the action execution.
        localization.thenConsume(future -> {
            if (future.hasError()) {
                String errorMessage = "LocalizeAndMap action finished with error.";
                Log.e(TAG, errorMessage, future.getError());
                displayLine(errorMessage, ConversationItemType.ERROR_LOG);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove on status changed listeners from the LocalizeAndMap action.
        if (localizeAndMap != null) {
            localizeAndMap.removeAllOnStatusChangedListeners();
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
