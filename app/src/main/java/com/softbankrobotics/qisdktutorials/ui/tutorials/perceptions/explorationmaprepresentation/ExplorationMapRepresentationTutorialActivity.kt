package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.explorationmaprepresentation

import android.os.Bundle
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity

/**
 * The tutorial for using ExplorationMap representation.
 */
class ExplorationMapRepresentationTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_exploration_map_representation_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {

    }

    override fun onRobotFocusLost() {
        // Nothing here.
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }
}
