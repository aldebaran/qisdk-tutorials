package com.softbankrobotics.qisdktutorials.model.repository;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory;
import com.softbankrobotics.qisdktutorials.model.data.TutorialId;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * The repository providing the tutorials.
 */
public class TutorialRepository {

    /**
     * Provide the tutorials for the specified category and level.
     * @param tutorialCategory the tutorial category
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the specified category and level.
     */
    public List<Tutorial> getTutorials(TutorialCategory tutorialCategory, TutorialLevel tutorialLevel) {
        switch (tutorialCategory) {
            case TALK:
                return getTalkTutorials(tutorialLevel);
            case MOVE:
                return getMoveTutorials(tutorialLevel);
            case SMART:
                return getSmartTutorials(tutorialLevel);
            default:
                throw new IllegalArgumentException("Unknown tutorial category: " + tutorialCategory);
        }
    }

    /**
     * Provide the tutorials for the talk category and the specified level.
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the talk category and the specified level.
     */
    private List<Tutorial> getTalkTutorials(TutorialLevel tutorialLevel) {
        List<Tutorial> tutorials = new ArrayList<>();

        switch (tutorialLevel) {
            case BASICS:
                tutorials.add(new Tutorial(TutorialId.SAY, R.string.hello_human, "hello", TutorialLevel.BASICS));
                tutorials.add(new Tutorial(TutorialId.DISCUSS, R.string.discuss, "discuss", TutorialLevel.BASICS));
                tutorials.add(new Tutorial(TutorialId.LISTEN, R.string.listen, "listen", TutorialLevel.BASICS));
                break;
            case ADVANCED:
                tutorials.add(new Tutorial(TutorialId.BOOKMARK, R.string.bookmarks, "bookmark", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.DYNAMIC_CONCEPT, R.string.dynamic_concepts, "dynamic_concept", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.QICHAT_VARIABLE, R.string.qichat_variables, "qichat_variable", TutorialLevel.ADVANCED));
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + tutorialLevel);
        }
        return tutorials;
    }

    /**
     * Provide the tutorials for the move category and the specified level.
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the move category and the specified level.
     */
    private List<Tutorial> getMoveTutorials(TutorialLevel tutorialLevel) {
        List<Tutorial> tutorials = new ArrayList<>();

        switch (tutorialLevel) {
            case BASICS:
                tutorials.add(new Tutorial(TutorialId.ANIMATION, R.string.animation, "animation", TutorialLevel.BASICS));
                tutorials.add(new Tutorial(TutorialId.TRAJECTORY, R.string.trajectory, "trajectory", TutorialLevel.BASICS));
                break;
            case ADVANCED:
                tutorials.add(new Tutorial(TutorialId.ANIMATION_LABEL, R.string.animation_label, "animation_label", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.GOTO, R.string.go_to, "goto", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.LOOKAT, R.string.look_at, "lookat", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.GOTO_WORLD, R.string.go_to_world, "goto_world", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.ATTACHED_FRAME, R.string.follow_human, "follow", TutorialLevel.ADVANCED));
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + tutorialLevel);
        }
        return tutorials;
    }

    /**
     * Provide the tutorials for the smart category and the specified level;
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the smart category and the specified level.
     */
    private List<Tutorial> getSmartTutorials(TutorialLevel tutorialLevel) {
        List<Tutorial> tutorials = new ArrayList<>();
        switch (tutorialLevel) {
            case BASICS:
                tutorials.add(new Tutorial(TutorialId.TOUCH, R.string.touch, "touch", TutorialLevel.BASICS));
                tutorials.add(new Tutorial(TutorialId.AUTONOMOUS_ABILITIES, R.string.autonomous_abilities, "autonomous", TutorialLevel.BASICS));
                tutorials.add(new Tutorial(TutorialId.TAKE_PICTURE, R.string.take_picture, "picture", TutorialLevel.BASICS));
                break;
            case ADVANCED:
                tutorials.add(new Tutorial(TutorialId.CHARACTERISTICS, R.string.people_characteristics, "people_characteristics", TutorialLevel.ADVANCED));
                tutorials.add(new Tutorial(TutorialId.EMOTION, R.string.emotion, "emotion", TutorialLevel.ADVANCED));
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + tutorialLevel);
        }
        return tutorials;
    }
}
