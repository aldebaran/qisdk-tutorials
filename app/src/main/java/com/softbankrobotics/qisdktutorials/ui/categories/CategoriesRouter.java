package com.softbankrobotics.qisdktutorials.ui.categories;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialId;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.animate.AnimateTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.animate.AnimationLabelActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.animate.TrajectoryTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.autonomousabilities.AutonomousAbilitiesTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.chat.BookmarksTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.chat.QiChatbotTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.chat.DynamicConceptsTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.chat.ExecuteTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.chat.QiChatVariablesTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.hellohuman.HelloHumanTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.listen.ListenTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.lookat.LookAtTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.move.FollowHumanTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.move.GoToTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.move.GoToWorldTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics.EmotionTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics.PeopleCharacteristicsTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.takepicture.TakePictureTutorialActivity;
import com.softbankrobotics.qisdktutorials.ui.tutorials.touch.TouchTutorialActivity;
import com.softbankrobotics.qisdktutorials.utils.Constants;

import static android.content.ContentValues.TAG;

/**
 * The router for the tutorial categories.
 */
class CategoriesRouter implements CategoriesContract.Router {

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
            case TAKE_PICTURE:
                return TakePictureTutorialActivity.class;
            case ANIMATION_LABEL:
                return AnimationLabelActivity.class;
            default:
                throw new IllegalArgumentException("Unknown tutorial identifier: " + tutorialId);
        }
    }
}
