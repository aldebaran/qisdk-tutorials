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
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.EditablePhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils;

import java.util.Collections;
import java.util.Map;

/**
 * The activity for the Dynamic concepts tutorial.
 */
public class DynamicConceptsTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private FavoriteAnimalAdapter favoriteAnimalAdapter;

    // Store the favorite animals dynamic concept.
    private EditablePhraseSet favoriteAnimals;
    private EditText animalEditText;
    // Store the ready BookmarkStatus.
    private BookmarkStatus readyBookmarkStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        animalEditText = findViewById(R.id.editText);
        animalEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleAddClick();
                }
                return false;
            }
        });

        // Create adapter for recycler view.
        favoriteAnimalAdapter = new FavoriteAnimalAdapter(new OnFavoriteAnimalClickedListener() {
            @Override
            public void onAnimalRemoved(String animalName) {
                // Remove animal from favorites.
                removeFavoriteAnimal(animalName);
                favoriteAnimalAdapter.removeAnimal(animalName);
            }
        });

        // Setup recycler view.
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(favoriteAnimalAdapter);

        // Add animal to favorites on add button clicked.
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
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.favorite_animals)
                .build();

        // Create a new discuss action.
        final Discuss discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the favorite animals dynamic concept.
        favoriteAnimals = discuss.dynamicConcept("favorite_animals");

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        final Bookmark readyBookmark = bookmarks.get("ready");
        final Bookmark favoriteBookmark = bookmarks.get("favorite");
        final Bookmark noFavoriteBookmark = bookmarks.get("no_favorite");

        // When user is ready, decide which proposal the robot should say.
        readyBookmarkStatus = discuss.bookmarkStatus(readyBookmark);
        readyBookmarkStatus.setOnReachedListener(new BookmarkStatus.OnReachedListener() {
            @Override
            public void onReached() {
                if (favoriteAnimals.getPhrases().isEmpty()) {
                    discuss.goToBookmarkedOutputUtterance(noFavoriteBookmark);
                } else {
                    discuss.goToBookmarkedOutputUtterance(favoriteBookmark);
                }
            }
        });

        // Run the discuss action asynchronously.
        discuss.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        // Remove the listener on the ready BookmarkStatus.
        if (readyBookmarkStatus != null) {
            readyBookmarkStatus.setOnReachedListener(null);
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void handleAddClick() {
        String animalName = animalEditText.getText().toString();
        animalEditText.setText("");
        KeyboardUtils.hideKeyboard(this);
        // Add animal only if new.
        if (!animalName.isEmpty() && !favoriteAnimalAdapter.containsAnimal(animalName)) {
            addFavoriteAnimal(animalName);
            favoriteAnimalAdapter.addAnimal(animalName);
        }
    }

    private void addFavoriteAnimal(String animalName) {
        // Add the animal name to the dynamic concept.
        if (favoriteAnimals != null) {
            favoriteAnimals.async().addPhrases(Collections.singletonList(new Phrase(animalName)));
        }
    }

    private void removeFavoriteAnimal(String animalName) {
        // Remove the animal name from the dynamic concept.
        if (favoriteAnimals != null) {
            favoriteAnimals.async().removePhrases(Collections.singletonList(new Phrase(animalName)));
        }
    }
}
