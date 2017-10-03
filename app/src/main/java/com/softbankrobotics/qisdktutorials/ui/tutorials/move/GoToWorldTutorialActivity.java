package com.softbankrobotics.qisdktutorials.ui.tutorials.move;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The activity for the Go to world tutorial.
 */
public class GoToWorldTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "GoToWorldActivity";

    private ConversationView conversationView;
    private Button goToButton;
    private Button saveButton;
    private ArrayAdapter<String> spinnerAdapter;

    // Store the selected location.
    private String selectedLocation;
    // Store the saved locations.
    private Map<String, FreeFrame> savedLocations = new HashMap<>();
    // The QiContext provided by the QiSDK.
    private QiContext qiContext;
    // Store the Actuation service.
    private Actuation actuation;
    // Store the Mapping service.
    private Mapping mapping;
    // Store the GoTo action.
    private GoTo goTo;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        editText = findViewById(R.id.editText);
        final Spinner spinner = findViewById(R.id.spinner);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleSaveClick();
                }
                return false;
            }
        });

        // Save location on save button clicked.
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveClick();
            }
        });

        // Go to location on go to button clicked.
        goToButton = findViewById(R.id.goto_button);
        goToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    goToButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    goToLocation(selectedLocation);
                }
            }
        });

        // Store location on selection.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "onItemSelected: " + selectedLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLocation = null;
                Log.i(TAG, "onNothingSelected");
            }
        });

        // Setup spinner adapter.
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

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
        return R.layout.activity_go_to_world_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "Focus gained.");
        // Store the provided QiContext and services.
        this.qiContext = qiContext;
        actuation = qiContext.getActuation();
        mapping = qiContext.getMapping();

        String textToSay = "I can store locations and go to them.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        waitForInstructions();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "Focus lost.");
        // Remove the QiContext.
        qiContext = null;
        // Remove the on started listener from the GoTo action.
        if (goTo != null) {
            goTo.setOnStartedListener(null);
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void handleSaveClick() {
        String location = editText.getText().toString();
        editText.setText("");
        KeyboardUtils.hideKeyboard(this);
        // Save location only if new.
        if (!location.isEmpty() && !savedLocations.containsKey(location)) {
            spinnerAdapter.add(location);
            displayLine("Location added: " + location, ConversationItemType.INFO_LOG);
            saveLocation(location);
        }
    }

    private void waitForInstructions() {
        String message = "Waiting for instructions...";
        Log.i(TAG, message);
        displayLine(message, ConversationItemType.INFO_LOG);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                saveButton.setEnabled(true);
                goToButton.setEnabled(true);
            }
        });
    }

    void saveLocation(final String location) {
        // Get the robot frame asynchronously.
        Future<Frame> robotFrameFuture = actuation.async().robotFrame();
        robotFrameFuture.andThenConsume(new Consumer<Frame>() {
            @Override
            public void consume(Frame robotFrame) throws Throwable {
                // Create a FreeFrame representing the current robot frame.
                FreeFrame locationFrame = mapping.makeFreeFrame();
                Transform transform = TransformBuilder.create().fromXTranslation(0);
                locationFrame.update(robotFrame, transform, System.currentTimeMillis());

                // Store the FreeFrame.
                savedLocations.put(location, locationFrame);
            }
        });
    }

    void goToLocation(final String location) {
        // Get the FreeFrame from the saved locations.
        FreeFrame freeFrame = savedLocations.get(location);

        // Extract the Frame asynchronously.
        Future<Frame> frameFuture = freeFrame.async().frame();
        frameFuture.andThenCompose(new Function<Frame, Future<Void>>() {
            @Override
            public Future<Void> execute(Frame frame) throws Throwable {
                // Create a GoTo action.
                goTo = GoToBuilder.with(qiContext)
                        .withFrame(frame)
                        .build();

                // Display text when the GoTo action starts.
                goTo.setOnStartedListener(new GoTo.OnStartedListener() {
                    @Override
                    public void onStarted() {
                        String message = "Moving...";
                        Log.i(TAG, message);
                        displayLine(message, ConversationItemType.INFO_LOG);
                    }
                });

                // Execute the GoTo action asynchronously.
                return goTo.async().run();
            }
        }).thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    Log.i(TAG, "Location reached: " + location);
                    waitForInstructions();
                } else if (future.hasError()) {
                    Log.e(TAG, "Go to location error", future.getError());
                    waitForInstructions();
                }
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
