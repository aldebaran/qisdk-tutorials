/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.EditablePhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder;
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
    private ConversationBinder conversationBinder;

    // Store the greetings dynamic concept.
    private EditablePhraseSet greetings;
    private EditText greetingEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        greetingEditText = findViewById(R.id.editText);
        greetingEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleAddClick();
            }
            return false;
        });

        // Create adapter for recycler view.
        // Remove greeting.
        greetingAdapter = new GreetingAdapter(this::removeGreeting);

        // Setup recycler view.
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(greetingAdapter);

        // Add greeting on add button clicked.
        ImageButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> handleAddClick());

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
        // Bind the conversational events to the view.
        ConversationStatus conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationBinder = conversationView.bindConversationTo(conversationStatus);

        Say say = SayBuilder.with(qiContext)
                .withText("Add more greetings to my dynamic concept and say \"Hello\".")
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
        Chat chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        // Get the greetings dynamic concept.
        greetings = qiChatbot.dynamicConcept("greetings");

        // Add default content to the dynamic concept.
        addGreeting("Hello");
        addGreeting("Hi");

        // Run the Chat action asynchronously.
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        if (conversationBinder != null) {
            conversationBinder.unbind();
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
            greetings.async().addPhrases(Collections.singletonList(new Phrase(greeting)))
                    .andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> greetingAdapter.addGreeting(greeting)));
        }
    }

    private void removeGreeting(final String greeting) {
        if (greetings != null) {
            greetings.async().removePhrases(Collections.singletonList(new Phrase(greeting)))
                    .andThenConsume(Qi.onUiThread((Consumer<Void>) ignore -> greetingAdapter.removeGreeting(greeting)));
        }
    }
}
