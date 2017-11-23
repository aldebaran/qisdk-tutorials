package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.EditablePhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

import java.util.Collections;

/**
 * The activity for the Dynamic concepts tutorial.
 */
public class DynamicConceptsTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private GreetingAdapter greetingAdapter;
    private ConversationView conversationView;

    // Store the greetings dynamic concept.
    private EditablePhraseSet greetings;
    private EditText greetingEditText;
    private Discuss discuss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        greetingEditText = findViewById(R.id.editText);
        greetingEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleAddClick();
                }
                return false;
            }
        });

        // Create adapter for recycler view.
        greetingAdapter = new GreetingAdapter(new OnGreetingRemovedListener() {
            @Override
            public void onGreetingRemoved(String greeting) {
                // Remove greeting.
                removeGreeting(greeting);
            }
        });

        // Setup recycler view.
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(greetingAdapter);

        // Add greeting on add button clicked.
        ImageButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddClick();
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
        return R.layout.activity_dynamic_concepts_tutorial;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        String textToSay = "Add more greetings to my dynamic concept and say \"Hello\".";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.greetings_dynamic)
                .build();

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the greetings dynamic concept.
        greetings = discuss.dynamicConcept("greetings");

        // Add default content to the dynamic concept.
        addGreeting("Hello");
        addGreeting("Hi");

        discuss.setOnLatestInputUtteranceChangedListener(new Discuss.OnLatestInputUtteranceChangedListener() {
            @Override
            public void onLatestInputUtteranceChanged(Phrase input) {
                displayLine(input.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });

        discuss.setOnLatestOutputUtteranceChangedListener(new Discuss.OnLatestOutputUtteranceChangedListener() {
            @Override
            public void onLatestOutputUtteranceChanged(Phrase output) {
                displayLine(output.getText(), ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Run the discuss action asynchronously.
        discuss.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (discuss != null) {
            discuss.setOnLatestInputUtteranceChangedListener(null);
            discuss.setOnLatestOutputUtteranceChangedListener(null);
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void handleAddClick() {
        String greeting = greetingEditText.getText().toString();
        greetingEditText.setText("");
        KeyboardUtils.hideKeyboard(this);
        // Add greeting only if new.
        if (!greeting.isEmpty() && !greetingAdapter.containsGreeting(greeting)) {
            addGreeting(greeting);
        }
    }

    private void addGreeting(final String greeting) {
        if (greetings != null) {
            greetings.async().addPhrases(Collections.singletonList(new Phrase(greeting))).andThenConsume(Qi.onUiThread(new Consumer<Void>() {
                @Override
                public void consume(Void ignore) throws Throwable {
                    greetingAdapter.addGreeting(greeting);
                }
            }));
        }
    }

    private void removeGreeting(final String greeting) {
        if (greetings != null) {
            greetings.async().removePhrases(Collections.singletonList(new Phrase(greeting))).andThenConsume(Qi.onUiThread(new Consumer<Void>() {
                @Override
                public void consume(Void ignore) throws Throwable {
                    greetingAdapter.removeGreeting(greeting);
                }
            }));
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
