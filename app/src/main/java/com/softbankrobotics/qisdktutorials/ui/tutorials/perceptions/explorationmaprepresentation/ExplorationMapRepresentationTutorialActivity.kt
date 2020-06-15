package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.explorationmaprepresentation

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
import com.aldebaran.qi.sdk.util.FutureUtils
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_exploration_map_representation_tutorial.*
import java.util.concurrent.TimeUnit

/**
 * The tutorial for using ExplorationMap representation.
 */
class ExplorationMapRepresentationTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var qiContext: QiContext? = null
    private var initialExplorationMap: ExplorationMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startMappingButton.setOnClickListener {
            qiContext?.let {
                startMappingButton.isEnabled = false
                mapSurroundings(it).thenConsume { future ->
                    if (future.isSuccess) {
                        val explorationMap = future.get()
                        this.initialExplorationMap = explorationMap
                        val bitmap = mapToBitmap(explorationMap)
                        runOnUiThread {
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                displayMap(bitmap)
                                extendMapButton.isEnabled = true
                            }
                        }
                    } else {
                        runOnUiThread {
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                startMappingButton.isEnabled = true
                            }
                        }
                    }
                }
            }
        }

        extendMapButton.setOnClickListener {
            val initialExplorationMap = initialExplorationMap ?: return@setOnClickListener
            val qiContext = qiContext ?: return@setOnClickListener
            extendMapButton.isEnabled = false
            extendMap(initialExplorationMap, qiContext) { updatedMap ->
                val updatedBitmap = mapToBitmap(updatedMap)
                runOnUiThread {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        displayMap(updatedBitmap)
                    }
                }
            }.thenConsume { future ->
                if (!future.isSuccess) {
                    runOnUiThread {
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            extendMapButton.isEnabled = true
                        }
                    }
                }
            }
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onResume() {
        super.onResume()
        startMappingButton.isEnabled = false
        extendMapButton.isEnabled = false
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_exploration_map_representation_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
        runOnUiThread {
            startMappingButton.isEnabled = true
        }
    }

    override fun onRobotFocusLost() {
        // Nothing here.
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun mapSurroundings(qiContext: QiContext): Future<ExplorationMap> {
        val promise = Promise<ExplorationMap>().apply {
            setOnCancel {
                if (!it.future.isDone) {
                    setCancelled()
                }
            }
        }

        val localizeAndMapFuture = LocalizeAndMapBuilder.with(qiContext)
                .buildAsync()
                .andThenCompose { localizeAndMap ->
                    localizeAndMap.addOnStatusChangedListener { status ->
                        if (status == LocalizationStatus.LOCALIZED) {
                            val explorationMap = localizeAndMap.dumpMap()
                            if (!promise.future.isDone) {
                                promise.setValue(explorationMap)
                            }
                        }
                    }

                    localizeAndMap.async().run().thenConsume {
                        localizeAndMap.removeAllOnStatusChangedListeners()
                        if (it.hasError() && !promise.future.isDone) {
                            promise.setError(it.errorMessage)
                        }
                    }
                }

        return promise.future.thenCompose {
            localizeAndMapFuture.cancel(true)
            return@thenCompose it
        }
    }

    private fun extendMap(explorationMap: ExplorationMap, qiContext: QiContext, updatedMapCallback: (ExplorationMap) -> Unit): Future<Void> {
        val promise = Promise<Void>().apply {
            setOnCancel {
                if (!it.future.isDone) {
                    setCancelled()
                }
            }
        }

        val localizeAndMapFuture = LocalizeAndMapBuilder.with(qiContext)
                .withMap(explorationMap)
                .buildAsync()
                .andThenCompose { localizeAndMap ->
                    var publishExplorationMapFuture: Future<Void>? = null

                    localizeAndMap.addOnStatusChangedListener { status ->
                        if (status == LocalizationStatus.LOCALIZED) {
                            publishExplorationMapFuture = publishExplorationMap(localizeAndMap, updatedMapCallback)
                        }
                    }

                    localizeAndMap.async().run().thenConsume {
                        localizeAndMap.removeAllOnStatusChangedListeners()
                        publishExplorationMapFuture?.cancel(true)
                        if (it.hasError() && !promise.future.isDone) {
                            promise.setError(it.errorMessage)
                        }
                    }
                }

        return promise.future.thenCompose {
            localizeAndMapFuture.cancel(true)
            return@thenCompose it
        }
    }

    private fun publishExplorationMap(localizeAndMap: LocalizeAndMap, updatedMapCallback: (ExplorationMap) -> Unit): Future<Void> {
        return localizeAndMap.async().dumpMap().andThenCompose {
            updatedMapCallback(it)
            FutureUtils.wait(2L, TimeUnit.SECONDS)
        }.andThenCompose {
            publishExplorationMap(localizeAndMap, updatedMapCallback)
        }
    }

    private fun mapToBitmap(explorationMap: ExplorationMap): Bitmap {
        val byteBuffer = explorationMap.topGraphicalRepresentation.image.data.apply { rewind() }
        val size = byteBuffer.remaining()
        val byteArray = ByteArray(size).also { byteBuffer.get(it) }
        return BitmapFactory.decodeByteArray(byteArray, 0, size)
    }

    private fun displayMap(bitmap: Bitmap) {
        mapImageView.setImageBitmap(bitmap)
    }
}
