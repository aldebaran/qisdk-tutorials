package com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.HumanInfo;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The activity for the People characteristics tutorial.
 */
public class PeopleCharacteristicsTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "CharacteristicsActivity";

    private ConversationView conversationView;

    private HumanInfoAdapter humanInfoAdapter;

    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;
    // The QiContext provided by the QiSDK.
    private QiContext qiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        humanInfoAdapter = new HumanInfoAdapter();
        recyclerView.setAdapter(humanInfoAdapter);

        // Find humans around when refresh button clicked.
        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qiContext != null) {
                    findHumansAround();
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
        return R.layout.activity_people_characteristics_tutorial;
    }

    @Override
    public void onRobotFocusGained(final QiContext qiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext;

        String textToSay = "I can display characteristics about the human I'm seeing.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Get the HumanAwareness service from the QiContext.
        humanAwareness = qiContext.getHumanAwareness();

        findHumansAround();
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

    private void findHumansAround() {
        if (humanAwareness != null) {
            // Get the humans around the robot.
            Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();

            humansAroundFuture.andThenConsume(new Consumer<List<Human>>() {
                @Override
                public void consume(List<Human> humansAround) throws Throwable {
                    Log.i(TAG, humansAround.size() + " human(s) around.");
                    retrieveCharacteristics(humansAround);
                }
            });
        }
    }

    private void retrieveCharacteristics(final List<Human> humans) {
        // Get the Actuation service from the QiContext.
        Actuation actuation = qiContext.getActuation();

        // Get the robot frame.
        Frame robotFrame = actuation.robotFrame();

        List<HumanInfo> humanInfoList = new ArrayList<>();
        for (int i = 0; i < humans.size(); i++) {
            // Get the human.
            Human human = humans.get(i);

            // Get the characteristics.
            Integer age = human.getEstimatedAge().getYears();
            Gender gender = human.getEstimatedGender();
            PleasureState pleasureState = human.getEmotion().getPleasure();
            ExcitementState excitementState = human.getEmotion().getExcitement();
            SmileState smileState = human.getFacialExpressions().getSmile();
            AttentionState attentionState = human.getAttention();
            Frame humanFrame = human.getHeadFrame();

            // Display the characteristics.
            Log.i(TAG, "----- Human " + i + " -----");
            Log.i(TAG, "Age: " + age + " year(s)");
            Log.i(TAG, "Gender: " + gender);
            Log.i(TAG, "Pleasure state: " + pleasureState);
            Log.i(TAG, "Excitement state: " + excitementState);
            Log.i(TAG, "Smile state: " + smileState);
            Log.i(TAG, "Attention state: " + attentionState);

            // Compute the distance.
            double distance = computeDistance(humanFrame, robotFrame);
            // Display the distance between the human and the robot.
            Log.i(TAG, "Distance: " + distance + " meter(s).");

            HumanInfo humanInfo = new HumanInfo(age, gender, pleasureState, excitementState, smileState, attentionState, distance);
            humanInfoList.add(humanInfo);
        }

        displayHumanInfoList(humanInfoList);
    }

    private double computeDistance(Frame humanFrame, Frame robotFrame) {
        // Get the TransformTime between the human frame and the robot frame.
        TransformTime transformTime = humanFrame.computeTransform(robotFrame);

        // Get the transform.
        Transform transform = transformTime.getTransform();

        // Get the translation.
        Vector3 translation = transform.getTranslation();

        // Get the x and y components of the translation.
        double x = translation.getX();
        double y = translation.getY();

        // Compute the distance and return it.
        return Math.sqrt(x * x + y * y);
    }

    private void displayHumanInfoList(final List<HumanInfo> humanInfoList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                humanInfoAdapter.updateList(humanInfoList);
            }
        });
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
