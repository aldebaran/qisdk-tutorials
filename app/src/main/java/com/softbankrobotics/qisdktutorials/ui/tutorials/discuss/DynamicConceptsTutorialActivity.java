package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
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
import java.util.Map;

/**
 * The activity for the Dynamic concepts tutorial.
 */
public class DynamicConceptsTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private ItemAdapter itemAdapter;
    private ConversationView conversationView;

    // Store the items dynamic concept.
    private EditablePhraseSet items;
    private EditText itemEditText;
    // Store the list BookmarkStatus.
    private BookmarkStatus listBookmarkStatus;
    private Discuss discuss;
    private Bookmark listBookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        itemEditText = findViewById(R.id.editText);
        itemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleAddClick();
                }
                return false;
            }
        });

        Button itemsButton = findViewById(R.id.items_button);
        itemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (discuss != null) {
                    discuss.async().goToBookmarkedOutputUtterance(listBookmark);
                }
            }
        });

        // Create adapter for recycler view.
        itemAdapter = new ItemAdapter(new OnItemRemovedListener() {
            @Override
            public void onItemRemoved(String itemName) {
                // Remove item.
                removeItem(itemName);
                itemAdapter.removeItem(itemName);
            }
        });

        // Setup recycler view.
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);

        // Add item on add button clicked.
        Button addButton = findViewById(R.id.add_button);
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
        String textToSay = "Add some items to the dynamic concept and say \"items\" to list them.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();

        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.items)
                .build();

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the items dynamic concept.
        items = discuss.dynamicConcept("items");

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        listBookmark = bookmarks.get("list");
        final Bookmark itemsBookmark = bookmarks.get("items");
        final Bookmark noItemBookmark = bookmarks.get("no_item");

        // When user is ready, decide which proposal the robot should say.
        listBookmarkStatus = discuss.bookmarkStatus(listBookmark);
        listBookmarkStatus.setOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                if (items.getPhrases().isEmpty()) {
                    discuss.goToBookmarkedOutputUtterance(noItemBookmark);
                } else {
                    discuss.goToBookmarkedOutputUtterance(itemsBookmark);
                }
            }
        });

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
        // Remove the listener on the list BookmarkStatus.
        if (listBookmarkStatus != null) {
            listBookmarkStatus.setOnReachedListener(null);
        }
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
        String itemName = itemEditText.getText().toString();
        itemEditText.setText("");
        KeyboardUtils.hideKeyboard(this);
        // Add item only if new.
        if (!itemName.isEmpty() && !itemAdapter.containsItem(itemName)) {
            addItem(itemName);
            itemAdapter.addItem(itemName);
        }
    }

    private void addItem(String itemName) {
        // Add the item name to the dynamic concept.
        if (items != null) {
            items.async().addPhrases(Collections.singletonList(new Phrase(itemName)));
        }
    }

    private void removeItem(String itemName) {
        // Remove the item name from the dynamic concept.
        if (items != null) {
            items.async().removePhrases(Collections.singletonList(new Phrase(itemName)));
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
