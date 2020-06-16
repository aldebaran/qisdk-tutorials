/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.takepicture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.Qi
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TakePictureBuilder
import com.aldebaran.qi.sdk.`object`.camera.TakePicture
import com.aldebaran.qi.sdk.`object`.image.TimestampedImageHandle
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_take_picture_tutorial.*

private const val TAG = "TakePictureActivity"

/**
 * The activity for the take picture tutorial.
 */
class TakePictureTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null

    private var pictureBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        take_pic_button.isEnabled = false
        take_pic_button.setOnClickListener { takePic() }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }


    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override val layoutId = R.layout.activity_take_picture_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversation_view.bindConversationTo(conversationStatus)

        runOnUiThread { take_pic_button.isEnabled = true }

        val say = SayBuilder.with(qiContext)
                .withText("I can take pictures. Press the button to try!")
                .build()

        say.run()
    }

    override fun onRobotFocusLost() {
        Log.i(TAG, "Focus lost.")
        // Remove the QiContext.
        this.qiContext = null

        conversationBinder?.unbind()
    }

    override fun onRobotFocusRefused(reason: String) {
        Log.i(TAG, "onRobotFocusRefused: $reason")
    }

    private fun takePic() {
        if (qiContext == null) {
            return
        }

        pictureBitmap?.let{
            it.recycle()
            pictureBitmap = null
            picture_view.setImageBitmap(null)
        }

        Log.i(TAG, "build take picture")
        // Build the action.
        val takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync()
        // Take picture
        takePictureFuture.andThenCompose<TimestampedImageHandle>(Qi.onUiThread<TakePicture, Future<TimestampedImageHandle>> { takePicture ->
            Log.i(TAG, "take picture launched!")
            progress_bar.visibility = View.VISIBLE
            take_pic_button.isEnabled = false
            takePicture.async().run()
        }).andThenConsume { timestampedImageHandle ->
            //Consume take picture action when it's ready
            Log.i(TAG, "Picture taken")
            // get picture
            val encodedImageHandle = timestampedImageHandle.image

            val encodedImage = encodedImageHandle.value
            Log.i(TAG, "PICTURE RECEIVED!")

            runOnUiThread {
                progress_bar.visibility = View.GONE
                take_pic_button.isEnabled = true
            }

            val buffer = encodedImage.data
            buffer.rewind()
            val pictureBufferSize = buffer.remaining()
            val pictureArray = ByteArray(pictureBufferSize)
            buffer.get(pictureArray)

            Log.i(TAG, "PICTURE RECEIVED! ($pictureBufferSize Bytes)")
            pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize)
            // display picture
            runOnUiThread { picture_view.setImageBitmap(pictureBitmap) }
        }
    }

}

