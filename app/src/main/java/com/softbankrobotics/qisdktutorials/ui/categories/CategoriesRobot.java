package com.softbankrobotics.qisdktutorials.ui.categories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

/**
 * The robot for the tutorial categories.
 */
class CategoriesRobot implements CategoriesContract.Robot, RobotLifecycleCallbacks {

    private static final String TAG = "CategoriesRobot";

    private static final String LEVEL_BASIC = "basic";
    private static final String LEVEL_ADVANCED = "advanced";

    private final CategoriesContract.Presenter presenter;
    private TopicStatus talkTopicStatus;
    private TopicStatus moveTopicStatus;
    private TopicStatus smartTopicStatus;
    private Discuss discuss;
    private Future<String> discussFuture;
    private TutorialCategory selectedCategory = TutorialCategory.TALK;
    private TutorialLevel selectedLevel = TutorialLevel.BASICS;
    private QiChatVariable levelVariable;

    CategoriesRobot(CategoriesContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void register(CategoriesActivity activity) {
        QiSDK.register(activity, this);
    }

    @Override
    public void unregister(CategoriesActivity activity) {
        QiSDK.unregister(activity, this);
    }

    @Override
    public void stopDiscussion(final Tutorial tutorial) {
        if (discussFuture != null) {
            discussFuture.thenConsume(new Consumer<Future<String>>() {
                @Override
                public void consume(Future<String> future) throws Throwable {
                    if (future.isCancelled()) {
                        presenter.goToTutorial(tutorial);
                    }
                }
            });
            discussFuture.requestCancellation();
        } else {
            presenter.goToTutorial(tutorial);
        }
    }

    @Override
    public void selectTopic(final TutorialCategory category) {
        selectedCategory = category;

        boolean topicsAreReady = talkTopicStatus != null && moveTopicStatus != null && smartTopicStatus != null;
        if (topicsAreReady) {
            enableTopic(category);
        }
    }

    @Override
    public void selectLevel(TutorialLevel level) {
        selectedLevel = level;

        if (levelVariable != null) {
            enableLevel(level);
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        SayBuilder.with(qiContext)
                .withText(qiContext.getString(R.string.categories_intro_sentence))
                .build()
                .run();

        final Topic commonTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.common)
                .build();

        Topic talkTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.talk_tutorials)
                .build();

        Topic moveTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.move_tutorials)
                .build();

        Topic smartTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.smart_tutorials)
                .build();

        discuss = DiscussBuilder.with(qiContext)
                .withTopic(commonTopic, talkTopic, moveTopic, smartTopic)
                .build();

        talkTopicStatus = discuss.topicStatus(talkTopic);
        moveTopicStatus = discuss.topicStatus(moveTopic);
        smartTopicStatus = discuss.topicStatus(smartTopic);

        levelVariable = discuss.variable("level");

        enableLevel(selectedLevel);
        enableTopic(selectedCategory);

        discuss.setOnBookmarkReachedListener(new Discuss.OnBookmarkReachedListener() {
            @Override
            public void onBookmarkReached(Bookmark bookmark) {
                String bookmarkName = bookmark.getName();
                switch (bookmarkName) {
                    case "talk":
                        presenter.loadTutorials(TutorialCategory.TALK);
                        selectTopic(TutorialCategory.TALK);
                        break;
                    case "move":
                        presenter.loadTutorials(TutorialCategory.MOVE);
                        selectTopic(TutorialCategory.MOVE);
                        break;
                    case "smart":
                        presenter.loadTutorials(TutorialCategory.SMART);
                        selectTopic(TutorialCategory.SMART);
                        break;
                    case "basics":
                        presenter.loadTutorials(TutorialLevel.BASICS);
                        selectLevel(TutorialLevel.BASICS);
                        break;
                    case "advanced":
                        presenter.loadTutorials(TutorialLevel.ADVANCED);
                        selectLevel(TutorialLevel.ADVANCED);
                        break;
                }
            }
        });

        discussFuture = discuss.async().run();

        discussFuture.andThenConsume(new Consumer<String>() {
            @Override
            public void consume(String tutorialDiscussId) throws Throwable {
                presenter.goToTutorialForDiscussId(tutorialDiscussId);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        if (discuss != null) {
            discuss.setOnBookmarkReachedListener(null);
            discuss = null;
        }
        discussFuture = null;
        talkTopicStatus = null;
        moveTopicStatus = null;
        smartTopicStatus = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);
    }

    /**
     * Enable the topic corresponding to the specified tutorial category.
     * @param category the tutorial category
     */
    private void enableTopic(final TutorialCategory category) {
        Future<Void> talkFuture = talkTopicStatus.async().setEnabled(false);
        Future<Void> moveFuture = moveTopicStatus.async().setEnabled(false);
        Future<Void> smartFuture = smartTopicStatus.async().setEnabled(false);

        Future.waitAll(talkFuture, moveFuture, smartFuture)
                .andThenConsume(new Consumer<Void>() {
                    @Override
                    public void consume(Void ignore) throws Throwable {
                        switch (category) {
                            case TALK:
                                talkTopicStatus.setEnabled(true);
                                break;
                            case MOVE:
                                moveTopicStatus.setEnabled(true);
                                break;
                            case SMART:
                                smartTopicStatus.setEnabled(true);
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown tutorial category: " + category);
                        }
                    }
                });
    }

    /**
     * Enable the specified level.
     * @param level the tutorial level
     */
    private void enableLevel(TutorialLevel level) {
        String value = levelValueFromLevel(level);
        levelVariable.async().setValue(value);
    }

    /**
     * Provides the level variable value from the specified tutorial level.
     * @param level the tutorial level
     * @return The level variable value.
     */
    @NonNull
    private String levelValueFromLevel(TutorialLevel level) {
        String value;
        switch (level) {
            case BASICS:
                value = LEVEL_BASIC;
                break;
            case ADVANCED:
                value = LEVEL_ADVANCED;
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + level);
        }
        return value;
    }
}
