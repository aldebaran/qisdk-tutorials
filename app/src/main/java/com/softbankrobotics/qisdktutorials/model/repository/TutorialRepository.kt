/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.model.repository

import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory
import com.softbankrobotics.qisdktutorials.model.data.TutorialId
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel

import java.util.ArrayList

/**
 * The repository providing the tutorials.
 */
class TutorialRepository {

    /**
     * Provide the tutorials for the specified category and level.
     * @param tutorialCategory the tutorial category
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the specified category and level.
     */
    fun getTutorials(tutorialCategory: TutorialCategory, tutorialLevel: TutorialLevel): List<Tutorial> {
        return when (tutorialCategory) {
            TutorialCategory.TALK -> getTalkTutorials(tutorialLevel)
            TutorialCategory.MOVE -> getMoveTutorials(tutorialLevel)
            TutorialCategory.SMART -> getSmartTutorials(tutorialLevel)
        }
    }

    /**
     * Provide the tutorials for the talk category and the specified level.
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the talk category and the specified level.
     */
    private fun getTalkTutorials(tutorialLevel: TutorialLevel): List<Tutorial> {
        val tutorials = mutableListOf<Tutorial>()

        when (tutorialLevel) {
            TutorialLevel.BASIC -> {
                tutorials.add(Tutorial(TutorialId.SAY, R.string.hello_human, "hello", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.QICHATBOT, R.string.qichatbot, "qichatbot", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.LISTEN, R.string.listen, "listen", TutorialLevel.BASIC))
            }
            TutorialLevel.ADVANCED -> {
                tutorials.add(Tutorial(TutorialId.BOOKMARK, R.string.bookmarks, "bookmark", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.EXECUTE, R.string.execute, "execute", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.DYNAMIC_CONCEPT, R.string.dynamic_concepts, "dynamic_concept", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.QICHAT_VARIABLE, R.string.qichat_variables, "qichat_variable", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.CHAT_LOCALE, R.string.chat_locale, "chat_locale", TutorialLevel.ADVANCED))
            }
        }
        return tutorials
    }

    /**
     * Provide the tutorials for the move category and the specified level.
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the move category and the specified level.
     */
    private fun getMoveTutorials(tutorialLevel: TutorialLevel): List<Tutorial> {
        val tutorials = mutableListOf<Tutorial>()

        when (tutorialLevel) {
            TutorialLevel.BASIC -> {
                tutorials.add(Tutorial(TutorialId.ANIMATION, R.string.animation, "animation", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.TRAJECTORY, R.string.trajectory, "trajectory", TutorialLevel.BASIC))
            }
            TutorialLevel.ADVANCED -> {
                tutorials.add(Tutorial(TutorialId.ANIMATION_LABEL, R.string.animation_label, "animation_label", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.GOTO, R.string.go_to, "goto", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.LOOKAT, R.string.look_at, "lookat", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.GOTO_WORLD, R.string.go_to_world, "goto_world", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.ATTACHED_FRAME, R.string.follow_human, "follow", TutorialLevel.ADVANCED))
            }
        }
        return tutorials
    }

    /**
     * Provide the tutorials for the smart category and the specified level;
     * @param tutorialLevel the tutorial level
     * @return The list of tutorials for the smart category and the specified level.
     */
    private fun getSmartTutorials(tutorialLevel: TutorialLevel): List<Tutorial> {
        val tutorials = ArrayList<Tutorial>()
        when (tutorialLevel) {
            TutorialLevel.BASIC -> {
                tutorials.add(Tutorial(TutorialId.TOUCH, R.string.touch, "touch", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.AUTONOMOUS_ABILITIES, R.string.autonomous_abilities, "autonomous", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.ENFORCE_TABLET_REACHABILITY, R.string.enforce_tablet_reachability, "enforce_tablet_reachability", TutorialLevel.BASIC))
                tutorials.add(Tutorial(TutorialId.TAKE_PICTURE, R.string.take_picture, "picture", TutorialLevel.BASIC))
            }
            TutorialLevel.ADVANCED -> {
                tutorials.add(Tutorial(TutorialId.CHARACTERISTICS, R.string.people_characteristics, "people_characteristics", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.EMOTION, R.string.emotion, "emotion", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.DETECT_HUMANS_WITH_LOCALIZATION, R.string.detect_humans_with_localization, "detect_humans_with_localization", TutorialLevel.ADVANCED))
                tutorials.add(Tutorial(TutorialId.EXPLORATION_MAP_REPRESENTATION, R.string.exploration_map_representation, "exploration_map_representation", TutorialLevel.ADVANCED))
            }
        }
        return tutorials
    }
}
