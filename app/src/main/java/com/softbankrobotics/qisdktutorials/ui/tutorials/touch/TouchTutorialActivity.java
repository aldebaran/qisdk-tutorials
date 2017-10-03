package com.softbankrobotics.qisdktutorials.ui.tutorials.touch;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.aldebaran.qi.sdk.object.touch.TouchState;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the Touch tutorial.
 */
public class TouchTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "TouchTutorialActivity";

    private ConversationView conversationView;

    // Store the head touch sensor.
    private TouchSensor headTouchSensor;

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
        String textToSay = "I have touch sensors: try to touch my head.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Get the Touch service from the QiContext.
        Touch touch = qiContext.getTouch();

        // Get the head touch sensor.
        headTouchSensor = touch.getSensor("Head/Touch");
        // Set onStateChanged listener.
        headTouchSensor.setOnStateChangedListener(new TouchSensor.OnStateChangedListener() {
            @Override
            public void onStateChanged(TouchState touchState) {
                String message = "Sensor " + (touchState.getTouched() ? "touched" : "released") + " at " + touchState.getTime();
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        // Remove onStateChanged listener.
        if (headTouchSensor != null) {
            headTouchSensor.setOnStateChangedListener(null);
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
