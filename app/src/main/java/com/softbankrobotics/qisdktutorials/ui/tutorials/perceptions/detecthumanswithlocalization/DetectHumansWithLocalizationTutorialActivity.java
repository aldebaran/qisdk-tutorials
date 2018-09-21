package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for detecting humans with localization.
 */
public class DetectHumansWithLocalizationTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "DetectHumansWithLoc";

    private ConversationView conversationView;
    private ConversationBinder conversationBinder;

    // Store the LocalizeAndMap action.
    private LocalizeAndMap localizeAndMap;
    // Store the map.
    private ExplorationMap explorationMap;
    // Store the LocalizeAndMap execution.
    private Future<Void> localizationAndMapping;
    // Store the Localize action.
    private Localize localize;

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
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        say(qiContext, "I will map my environment. Please be sure that my hatch is closed and that you are at least 3 meters away from me while I'm scanning the place.");
        say(qiContext, "Ready? 5, 4, 3, 2, 1.");

        startMapping(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        if (conversationBinder != null) {
            conversationBinder.unbind();
        }

        // Remove on status changed listeners from the LocalizeAndMap action.
        if (localizeAndMap != null) {
            localizeAndMap.removeAllOnStatusChangedListeners();
        }
        // Remove on status changed listeners from the Localize action.
        if (localize != null) {
            localize.removeAllOnStatusChangedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void startMapping(QiContext qiContext) {
        // Create a LocalizeAndMap action.
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

        // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
        localizeAndMap.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    // Dump the ExplorationMap.
                    explorationMap = localizeAndMap.dumpMap();

                    String message = "Robot has mapped his environment.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);

                    say(qiContext, "I now have a map of my environment. I will use this map to localize myself.");

                    // Cancel the LocalizeAndMap action.
                    localizationAndMapping.requestCancellation();
                    break;
            }
        });

        String message = "Mapping...";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);

        // Execute the LocalizeAndMap action asynchronously.
        localizationAndMapping = localizeAndMap.async().run();

        // Add a lambda to the action execution.
        localizationAndMapping.thenConsume(future -> {
            if (future.hasError()) {
                String errorMessage = "LocalizeAndMap action finished with error.";
                Log.e(TAG, errorMessage, future.getError());
                displayLine(errorMessage, ConversationItemType.ERROR_LOG);
            } else if (future.isCancelled()) {
                startLocalizing(qiContext);
            }
        });
    }

    private void startLocalizing(QiContext qiContext) {
        // Create a Localize action.
        localize = LocalizeBuilder.with(qiContext)
                .withMap(explorationMap)
                .build();

        // Add an on status changed listener on the Localize action for the robot to say when he is localized.
        localize.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    String message = "Robot is localized.";
                    Log.i(TAG, message);
                    displayLine(message, ConversationItemType.INFO_LOG);

                    say(qiContext, "I'm now localized and I have a 360° awareness thanks to my base sensors. Try to come from behind and I will detect you.");
                    break;
            }
        });

        String message = "Localizing...";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);

        // Execute the Localize action asynchronously.
        Future<Void> localization = localize.async().run();

        // Add a lambda to the action execution.
        localization.thenConsume(future -> {
            if (future.hasError()) {
                String errorMessage = "Localize action finished with error.";
                Log.e(TAG, errorMessage, future.getError());
                displayLine(errorMessage, ConversationItemType.ERROR_LOG);
            }
        });
    }

    private void say(QiContext qiContext, String text) {
        SayBuilder.with(qiContext)
                .withText(text)
                .build()
                .run();
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(() -> conversationView.addLine(text, type));
    }
}
