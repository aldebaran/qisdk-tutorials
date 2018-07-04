package com.softbankrobotics.qisdktutorials.ui.tutorials.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.EditablePhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
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
    private Chat chat;

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
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
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

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        // Get the greetings dynamic concept.
        greetings = qiChatbot.dynamicConcept("greetings");

        // Add default content to the dynamic concept.
        addGreeting("Hello");
        addGreeting("Hi");

        chat.addOnHeardListener(new Chat.OnHeardListener() {
            @Override
            public void onHeard(Phrase heardPhrase) {
                displayLine(heardPhrase.getText(), ConversationItemType.HUMAN_INPUT);
            }
        });

        chat.addOnSayingChangedListener(new Chat.OnSayingChangedListener() {
            @Override
            public void onSayingChanged(Phrase sayingPhrase) {
                String text = sayingPhrase.getText();
                if (!TextUtils.isEmpty(text)) {
                    displayLine(text, ConversationItemType.ROBOT_OUTPUT);
                }
            }
        });

        // Run the Chat action asynchronously.
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (chat != null) {
            chat.removeAllOnHeardListeners();
            chat.removeAllOnSayingChangedListeners();
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
