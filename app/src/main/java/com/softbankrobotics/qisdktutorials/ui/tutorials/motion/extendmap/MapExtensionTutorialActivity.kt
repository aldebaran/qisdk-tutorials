package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.extendmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.aldebaran.qi.Future
import com.aldebaran.qi.Promise
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.ExplorationMap
import com.aldebaran.qi.sdk.`object`.actuation.LocalizationStatus
import com.aldebaran.qi.sdk.`object`.actuation.LocalizeAndMap
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.util.FutureUtils
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_exploration_map_representation_tutorial.*
import java.util.concurrent.TimeUnit

/**
 * The tutorial to extend an ExplorationMap.
 */
class MapExtensionTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null
    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null
    // The initial ExplorationMap.
    private var initialExplorationMap: ExplorationMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the button onClick listener.
        startMappingButton.setOnClickListener {
            // Check that the Activity owns the focus.
            val qiContext = qiContext ?: return@setOnClickListener
            // Start the mapping step.
            startMappingStep(qiContext)
        }

        extendMapButton.setOnClickListener {
            // Check that an initial map is available.
            val initialExplorationMap = initialExplorationMap ?: return@setOnClickListener
            // Check that the Activity owns the focus.
            val qiContext = qiContext ?: return@setOnClickListener
            // Start the map extension step.
            startMapExtensionStep(initialExplorationMap, qiContext)
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onResume() {
        super.onResume()
        // Reset UI and variables state.
        startMappingButton.isEnabled = false
        extendMapButton.isEnabled = false
        initialExplorationMap = null
        mapImageView.setImageBitmap(null)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_exploration_map_representation_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can display the graphical representation of a map. Click on start mapping to try!")
                .build()

        say.run()

        // Enable "start mapping" button.
        runOnUiThread {
            startMappingButton.isEnabled = true
        }
    }

    override fun onRobotFocusLost() {
        conversationBinder?.unbind()

        // Remove the QiContext.
        this.qiContext = null
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun startMappingStep(qiContext: QiContext) {
        displayLine("Start mapping...", ConversationItemType.INFO_LOG)
        // Disable "start mapping" button.
        startMappingButton.isEnabled = false
        // Map the surroundings and get the map.
        mapSurroundings(qiContext).thenConsume { future ->
            if (future.isSuccess) {
                displayLine("Mapping done, retrieving map representation...", ConversationItemType.INFO_LOG)
                val explorationMap = future.get()
                // Store the initial map.
                this.initialExplorationMap = explorationMap
                // Convert the map to a bitmap.
                val bitmap = mapToBitmap(explorationMap)
                displayLine("Map representation retrieved.", ConversationItemType.INFO_LOG)
                // Display the bitmap and enable "extend map" button.
                runOnUiThread {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        displayMap(bitmap)
                        extendMapButton.isEnabled = true
                    }
                }

                val say = SayBuilder.with(qiContext)
                        .withText("I can now extend this map. Click on extend map, then give me a tour to try!")
                        .build()

                say.run()
            } else {
                if (future.hasError()) {
                    displayLine("Mapping failed: ${future.errorMessage}", ConversationItemType.ERROR_LOG)
                }
                // If the operation is not a success, re-enable "start mapping" button.
                runOnUiThread {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        startMappingButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun startMapExtensionStep(initialExplorationMap: ExplorationMap, qiContext: QiContext) {
        displayLine("Starting map extension...", ConversationItemType.INFO_LOG)
        // Disable "extend map" button.
        extendMapButton.isEnabled = false
        // Start the map extension and notify each time the map is updated.
        extendMap(initialExplorationMap, qiContext) { updatedMap ->
            // Convert the map to a bitmap.
            val updatedBitmap = mapToBitmap(updatedMap)
            displayLine("New map representation available.", ConversationItemType.INFO_LOG)
            // Display the bitmap.
            runOnUiThread {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    displayMap(updatedBitmap)
                }
            }
        }.thenConsume { future ->
            // If the operation is not a success, re-enable "extend map" button.
            if (!future.isSuccess) {
                if (future.hasError()) {
                    displayLine("Map extension failed: ${future.errorMessage}", ConversationItemType.ERROR_LOG)
                }
                runOnUiThread {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        extendMapButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun mapSurroundings(qiContext: QiContext): Future<ExplorationMap> {
        // Create a Promise to set the operation state later.
        val promise = Promise<ExplorationMap>().apply {
            // If something tries to cancel the associated Future, do cancel it.
            setOnCancel {
                if (!it.future.isDone) {
                    setCancelled()
                }
            }
        }

        // Create a LocalizeAndMap, run it, and keep the Future.
        val localizeAndMapFuture = LocalizeAndMapBuilder.with(qiContext)
                .buildAsync()
                .andThenCompose { localizeAndMap ->
                    // Add an OnStatusChangedListener to know when the robot is localized.
                    localizeAndMap.addOnStatusChangedListener { status ->
                        if (status == LocalizationStatus.LOCALIZED) {
                            // Retrieve the map.
                            val explorationMap = localizeAndMap.dumpMap()
                            // Set the Promise state in success, with the ExplorationMap.
                            if (!promise.future.isDone) {
                                promise.setValue(explorationMap)
                            }
                        }
                    }

                    // Run the LocalizeAndMap.
                    localizeAndMap.async().run().thenConsume {
                        // Remove the OnStatusChangedListener.
                        localizeAndMap.removeAllOnStatusChangedListeners()
                        // In case of error, forward it to the Promise.
                        if (it.hasError() && !promise.future.isDone) {
                            promise.setError(it.errorMessage)
                        }
                    }
                }

        // Return the Future associated to the Promise.
        return promise.future.thenCompose {
            // Stop the LocalizeAndMap.
            localizeAndMapFuture.cancel(true)
            return@thenCompose it
        }
    }

    private fun extendMap(explorationMap: ExplorationMap, qiContext: QiContext, updatedMapCallback: (ExplorationMap) -> Unit): Future<Void> {
        // Create a Promise to set the operation state later.
        val promise = Promise<Void>().apply {
            // If something tries to cancel the associated Future, do cancel it.
            setOnCancel {
                if (!it.future.isDone) {
                    setCancelled()
                }
            }
        }

        // Create a LocalizeAndMap with the initial map, run it, and keep the Future.
        val localizeAndMapFuture = LocalizeAndMapBuilder.with(qiContext)
                .withMap(explorationMap)
                .buildAsync()
                .andThenCompose { localizeAndMap ->
                    // Create a Future for map notification.
                    var publishExplorationMapFuture: Future<Void>? = null

                    // Add an OnStatusChangedListener to know when the robot is localized.
                    localizeAndMap.addOnStatusChangedListener { status ->
                        if (status == LocalizationStatus.LOCALIZED) {
                            // Start the map notification process.
                            publishExplorationMapFuture = publishExplorationMap(localizeAndMap, updatedMapCallback)
                        }
                    }

                    // Run the LocalizeAndMap.
                    localizeAndMap.async().run().thenConsume {
                        // Remove the OnStatusChangedListener.
                        localizeAndMap.removeAllOnStatusChangedListeners()
                        // Stop the map notification process.
                        publishExplorationMapFuture?.cancel(true)
                        // In case of error, forward it to the Promise.
                        if (it.hasError() && !promise.future.isDone) {
                            promise.setError(it.errorMessage)
                        }
                    }
                }

        // Return the Future associated to the Promise.
        return promise.future.thenCompose {
            // Stop the LocalizeAndMap.
            localizeAndMapFuture.cancel(true)
            return@thenCompose it
        }
    }

    private fun publishExplorationMap(localizeAndMap: LocalizeAndMap, updatedMapCallback: (ExplorationMap) -> Unit): Future<Void> {
        // Retrieve the map.
        return localizeAndMap.async().dumpMap().andThenCompose {
            // Call the callback with the map.
            updatedMapCallback(it)
            // Wait for 2 seconds.
            FutureUtils.wait(2L, TimeUnit.SECONDS)
        }.andThenCompose {
            // Call the method recursively.
            publishExplorationMap(localizeAndMap, updatedMapCallback)
        }
    }

    private fun mapToBitmap(explorationMap: ExplorationMap): Bitmap {
        // Get the ByteBuffer containing the map graphical representation.
        val byteBuffer = explorationMap.topGraphicalRepresentation.image.data.apply { rewind() }
        // Get the buffer size.
        val size = byteBuffer.remaining()
        // Transform the buffer to a ByteArray.
        val byteArray = ByteArray(size).also { byteBuffer.get(it) }
        // Transform the ByteArray to a Bitmap.
        return BitmapFactory.decodeByteArray(byteArray, 0, size)
    }

    private fun displayMap(bitmap: Bitmap) {
        // Set the ImageView bitmap.
        mapImageView.setImageBitmap(bitmap)
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                conversation_view.addLine(text, type)
            }
        }
    }
}
