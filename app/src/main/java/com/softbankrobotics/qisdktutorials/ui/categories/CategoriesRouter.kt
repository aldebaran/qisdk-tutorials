/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import android.content.Context
import android.content.Intent
import android.util.Log

import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialId
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.autonomousabilities.AutonomousAbilitiesTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.bookmarks.BookmarksTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.chatlocale.ChatLocaleTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.dynamicconcepts.DynamicConceptsTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.execute.ExecuteTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.listen.ListenTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatbot.QiChatbotTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.conversation.qichatvariables.QiChatVariablesTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.gettingstarted.HelloHumanTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animate.AnimateTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.animationlabels.AnimationLabelActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.attachedframes.FollowHumanTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.enforcetabletreachability.EnforceTabletReachabilityTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.freeframes.GoToWorldTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.gotoframe.GoToTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.lookat.LookAtTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.motion.trajectory.TrajectoryTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization.DetectHumansWithLocalizationTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.emotiondetection.EmotionTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness.PeopleCharacteristicsTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.takepicture.TakePictureTutorialActivity
import com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.touch.TouchTutorialActivity
import com.softbankrobotics.qisdktutorials.utils.Constants

private const val TAG = "CategoriesRouter"

/**
 * The router for the tutorial categories.
 */
internal class CategoriesRouter : CategoriesContract.Router {

    override fun goToTutorial(tutorial: Tutorial, context: Context) {
        val tutorialId = tutorial.id
        val intent = Intent(context, getDestinationActivity(tutorialId))
        intent.putExtra(Constants.Intent.TUTORIAL_NAME_KEY, tutorial.nameResId)
        intent.putExtra(Constants.Intent.TUTORIAL_LEVEL_KEY, tutorial.tutorialLevel)
        context.startActivity(intent)
    }

    /**
     * Provide the destination activity class for the specified tutorial identifier.
     * @param tutorialId the tutorial identifier
     * @return The destination activity class.
     */
    private fun getDestinationActivity(tutorialId: TutorialId): Class<out TutorialActivity> {
        Log.i(TAG, "tutorialId: $tutorialId")
        return when (tutorialId) {
            TutorialId.SAY -> HelloHumanTutorialActivity::class.java
            TutorialId.QICHATBOT -> QiChatbotTutorialActivity::class.java
            TutorialId.LISTEN -> ListenTutorialActivity::class.java
            TutorialId.GOTO -> GoToTutorialActivity::class.java
            TutorialId.TOUCH -> TouchTutorialActivity::class.java
            TutorialId.LOOKAT -> LookAtTutorialActivity::class.java
            TutorialId.ANIMATION -> AnimateTutorialActivity::class.java
            TutorialId.TRAJECTORY -> TrajectoryTutorialActivity::class.java
            TutorialId.BOOKMARK -> BookmarksTutorialActivity::class.java
            TutorialId.EXECUTE -> ExecuteTutorialActivity::class.java
            TutorialId.ATTACHED_FRAME -> FollowHumanTutorialActivity::class.java
            TutorialId.CHARACTERISTICS -> PeopleCharacteristicsTutorialActivity::class.java
            TutorialId.DYNAMIC_CONCEPT -> DynamicConceptsTutorialActivity::class.java
            TutorialId.QICHAT_VARIABLE -> QiChatVariablesTutorialActivity::class.java
            TutorialId.GOTO_WORLD -> GoToWorldTutorialActivity::class.java
            TutorialId.AUTONOMOUS_ABILITIES -> AutonomousAbilitiesTutorialActivity::class.java
            TutorialId.EMOTION -> EmotionTutorialActivity::class.java
            TutorialId.ENFORCE_TABLET_REACHABILITY -> EnforceTabletReachabilityTutorialActivity::class.java
            TutorialId.TAKE_PICTURE -> TakePictureTutorialActivity::class.java
            TutorialId.ANIMATION_LABEL -> AnimationLabelActivity::class.java
            TutorialId.DETECT_HUMANS_WITH_LOCALIZATION -> DetectHumansWithLocalizationTutorialActivity::class.java
            TutorialId.CHAT_LOCALE -> ChatLocaleTutorialActivity::class.java
        }
    }
}
