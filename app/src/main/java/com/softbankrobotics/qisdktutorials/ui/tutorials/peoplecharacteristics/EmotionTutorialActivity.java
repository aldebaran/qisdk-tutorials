package com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.BasicEmotion;
import com.softbankrobotics.qisdktutorials.model.observer.BasicEmotionObserver;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for the Emotion tutorial.
 */
public class EmotionTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks, OnBasicEmotionChangedListener {

    // Store the basic emotion observer.
    private BasicEmotionObserver basicEmotionObserver;

    private ConversationView conversationView;
    private ImageView emotionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);
        emotionView = findViewById(R.id.emotionView);

        // Create the basic emotion observer and listen to it.
        basicEmotionObserver = new BasicEmotionObserver();
        basicEmotionObserver.setListener(this);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Stop listening to basic emotion observer and remove it.
        basicEmotionObserver.setListener(null);
        basicEmotionObserver = null;

        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_emotion_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "I can display the basic emotions of the human I'm seeing. Try to express an emotion with your smile, your voice or by touching my sensors.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Start the basic emotion observation.
        basicEmotionObserver.startObserving(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        // Stop the basic emotion observation.
        basicEmotionObserver.stopObserving();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    @Override
    public void onBasicEmotionChanged(final BasicEmotion basicEmotion) {
        // Update basic emotion image.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emotionView.setImageResource(emotionImageRes(basicEmotion));
            }
        });
    }

    @DrawableRes
    private int emotionImageRes(BasicEmotion basicEmotion) {
        switch (basicEmotion) {
            case UNKNOWN:
                return R.drawable.ic_icons_cute_anon_unknown;
            case NEUTRAL:
                return R.drawable.ic_icons_cute_anon_neutral;
            case CONTENT:
                return R.drawable.ic_icons_cute_anon_smile;
            case JOYFUL:
                return R.drawable.ic_icons_cute_anon_joyful;
            case SAD:
                return R.drawable.ic_icons_cute_anon_sad;
            case ANGRY:
                return R.drawable.ic_icons_cute_anon_anger;
            default:
                throw new IllegalArgumentException("Unknown basic emotion: " + basicEmotion);
        }
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
