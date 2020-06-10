package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.explorationmaprepresentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.Future
import com.aldebaran.qi.Promise
import com.aldebaran.qi.sdk.Qi
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.ExplorationMap
import com.aldebaran.qi.sdk.`object`.actuation.LocalizationStatus
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_exploration_map_representation_tutorial.*

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
        mapSurroundings(qiContext).andThenApply {
            mapToBitmap(it)
        }.andThenConsume(Qi.onUiThread(Consumer {
            displayMap(it)
        }))
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
                        if (it.hasError()) {
                            if (!promise.future.isDone) {
                                promise.setError(it.errorMessage)
                            }
                        }
                    }
                }

        return promise.future.thenCompose {
            localizeAndMapFuture.cancel(true)
            return@thenCompose it
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
