package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.detecthumanswithlocalization

import android.os.Bundle
import android.util.Log

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder
import com.aldebaran.qi.sdk.builder.LocalizeBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.ExplorationMap
import com.aldebaran.qi.sdk.`object`.actuation.LocalizationStatus.*
import com.aldebaran.qi.sdk.`object`.actuation.Localize
import com.aldebaran.qi.sdk.`object`.actuation.LocalizeAndMap
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_autonomous_abilities_tutorial.*

private const val TAG = "DetectHumansWithLoc"

/**
 * The activity for detecting humans with localization.
 */
class DetectHumansWithLocalizationTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private lateinit var conversationBinder: ConversationBinder

    // Store the LocalizeAndMap action.
    private lateinit var localizeAndMap: LocalizeAndMap
    // Store the map.
    private var explorationMap: ExplorationMap? = null
    // Store the LocalizeAndMap execution.
    private lateinit var localizationAndMapping: Future<Void>
    // Store the Localize action.
    private lateinit var localize: Localize

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

    override val layoutId = R.layout.activity_detect_humans_with_localization_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        say(qiContext, "I will map my environment. Please be sure that my hatch is closed and that you are at least 3 meters away from me while I'm scanning the place.")
        say(qiContext, "Ready? 5, 4, 3, 2, 1.")

        startMapping(qiContext)
    }

    override fun onRobotFocusLost() {
        conversationBinder.unbind()

        // Remove on status changed listeners from the LocalizeAndMap action.
        localizeAndMap.removeAllOnStatusChangedListeners()

        // Remove on status changed listeners from the Localize action.
        localize.removeAllOnStatusChangedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun startMapping(qiContext: QiContext) {
        // Create a LocalizeAndMap action.
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext).build()

        // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
        localizeAndMap.addOnStatusChangedListener { status ->
             if (status == LOCALIZED) {
                        // Dump the ExplorationMap.
                        explorationMap = localizeAndMap.dumpMap()

                        val message = "Robot has mapped his environment."
                        Log.i(TAG, message)
                        displayLine(message, ConversationItemType.INFO_LOG)

                        say(qiContext, "I now have a map of my environment. I will use this map to localize myself.")

                        // Cancel the LocalizeAndMap action.
                        localizationAndMapping.requestCancellation()
             }
        }

        val message = "Mapping..."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)

        // Execute the LocalizeAndMap action asynchronously.
        localizationAndMapping = localizeAndMap.async().run()

        // Add a lambda to the action execution.
        localizationAndMapping.thenConsume { future ->
            if (future.hasError()) {
                val errorMessage = "LocalizeAndMap action finished with error."
                Log.e(TAG, errorMessage, future.error)
                displayLine(errorMessage, ConversationItemType.ERROR_LOG)
            } else if (future.isCancelled) {
                startLocalizing(qiContext)
            }
        }
    }

    private fun startLocalizing(qiContext: QiContext) {
        // Create a Localize action.
        localize = LocalizeBuilder.with(qiContext)
                .withMap(explorationMap)
                .build()

        // Add an on status changed listener on the Localize action for the robot to say when he is localized.
        localize.addOnStatusChangedListener { status ->
            if (status == LOCALIZED) {
                    val message = "Robot is localized."
                    Log.i(TAG, message)
                    displayLine(message, ConversationItemType.INFO_LOG)

                    say(qiContext, "I'm now localized and I have a 360° awareness thanks to my base sensors. Try to come from behind and I will detect you.")
            }
        }

        val message = "Localizing..."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)

        // Execute the Localize action asynchronously.
        val localization = localize.async().run()

        // Add a lambda to the action execution.
        localization?.thenConsume { future ->
            if (future.hasError()) {
                val errorMessage = "Localize action finished with error."
                Log.e(TAG, errorMessage, future.error)
                displayLine(errorMessage, ConversationItemType.ERROR_LOG)
            }
        }
    }

    private fun say(qiContext: QiContext, text: String) {
        SayBuilder.with(qiContext)
                .withText(text)
                .build()
                .run()
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversation_view.addLine(text, type) }
    }
}
