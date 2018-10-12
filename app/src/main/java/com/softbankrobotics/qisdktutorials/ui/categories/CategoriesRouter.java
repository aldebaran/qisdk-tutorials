/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialId;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.autonomousabilities.AutonomousAbilitiesTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.bookmarks.BookmarksTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.chatlocale.ChatLocaleTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts.DynamicConceptsTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.execute.ExecuteTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.listen.ListenTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatbot.QiChatbotTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatvariables.QiChatVariablesTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.gettingstarted.HelloHumanTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animate.AnimateTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animationlabels.AnimationLabelActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.attachedframes.FollowHumanTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.enforcetabletreachability.EnforceTabletReachabilityTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.freeframes.GoToWorldTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.gotoframe.GoToTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.lookat.LookAtTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.trajectory.TrajectoryTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization.DetectHumansWithLocalizationTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.emotiondetection.EmotionTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness.PeopleCharacteristicsTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.takepicture.TakePictureTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.touch.TouchTutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.Constants;


/**
 * The router for the tutorial categories.
 */
class CategoriesRouter implements CategoriesContract.Router {

    private static final String TAG = "CategoriesRouter";

    @Override
    public void goToTutorial(Tutorial tutorial, Context context) {
        TutorialId tutorialId = tutorial.getId();
        Intent intent = new Intent(context, getDestinationActivity(tutorialId));
        intent.putExtra(Constants.Intent.TUTORIAL_NAME_KEY, tutorial.getNameResId());
        intent.putExtra(Constants.Intent.TUTORIAL_LEVEL_KEY, tutorial.getTutorialLevel());
        context.startActivity(intent);
    }

    /**
     * Provide the destination activity class for the specified tutorial identifier.
     * @param tutorialId the tutorial identifier
     * @return The destination activity class.
     */
    private Class<? extends TutorialActivity> getDestinationActivity(TutorialId tutorialId) {
        Log.i(TAG, "tutorialId: " + tutorialId);
        switch (tutorialId) {
            case SAY:
                return HelloHumanTutorialActivity.class;
            case QICHATBOT:
                return QiChatbotTutorialActivity.class;
            case LISTEN:
                return ListenTutorialActivity.class;
            case GOTO:
                return GoToTutorialActivity.class;
            case TOUCH:
                return TouchTutorialActivity.class;
            case LOOKAT:
                return LookAtTutorialActivity.class;
            case ANIMATION:
                return AnimateTutorialActivity.class;
            case TRAJECTORY:
                return TrajectoryTutorialActivity.class;
            case BOOKMARK:
                return BookmarksTutorialActivity.class;
            case EXECUTE:
                return ExecuteTutorialActivity.class;
            case ATTACHED_FRAME:
                return FollowHumanTutorialActivity.class;
            case CHARACTERISTICS:
                return PeopleCharacteristicsTutorialActivity.class;
            case DYNAMIC_CONCEPT:
                return DynamicConceptsTutorialActivity.class;
            case QICHAT_VARIABLE:
                return QiChatVariablesTutorialActivity.class;
            case GOTO_WORLD:
                return GoToWorldTutorialActivity.class;
            case AUTONOMOUS_ABILITIES:
                return AutonomousAbilitiesTutorialActivity.class;
            case EMOTION:
                return EmotionTutorialActivity.class;
            case ENFORCE_TABLET_REACHABILITY:
                return EnforceTabletReachabilityTutorialActivity.class;
            case TAKE_PICTURE:
                return TakePictureTutorialActivity.class;
            case ANIMATION_LABEL:
                return AnimationLabelActivity.class;
            case DETECT_HUMANS_WITH_LOCALIZATION:
                return DetectHumansWithLocalizationTutorialActivity.class;
            case CHAT_LOCALE:
                return ChatLocaleTutorialActivity.class;
            default:
                throw new IllegalArgumentException("Unknown tutorial identifier: " + tutorialId);
        }
    }
}
