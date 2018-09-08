package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization;

import android.os.Bundle;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

/**
 * The activity for detecting humans with localization.
 */
public class DetectHumansWithLocalizationTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private LocalizeAndMap localizeAndMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        SayBuilder.with(qiContext)
                .withText("I will start my localization. Please be sure to be at least at 3 meters away from me while I scan the place.")
                .build()
                .run();

        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build();

        localizeAndMap.addOnStatusChangedListener(status -> {
            switch (status) {
                case LOCALIZED:
                    SayBuilder.with(qiContext)
                            .withText("I'm now localized. Try to come to me from the side and I will turn towards you.")
                            .build()
                            .run();
                    break;
            }
        });

        localizeAndMap.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (localizeAndMap != null) {
            localizeAndMap.removeAllOnStatusChangedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }
}
