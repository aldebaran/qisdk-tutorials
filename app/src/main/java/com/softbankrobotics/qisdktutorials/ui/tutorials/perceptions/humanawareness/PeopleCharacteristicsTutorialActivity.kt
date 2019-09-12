/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import kotlinx.android.synthetic.main.activity_people_characteristics_tutorial.*

import java.util.ArrayList
import kotlin.math.sqrt

private const val TAG = "CharacteristicsActivity"


/**
 * The activity for the People characteristics tutorial.
 */
class PeopleCharacteristicsTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null

    private lateinit var humanInfoAdapter: HumanInfoAdapter

    // Store the HumanAwareness service.
    private var humanAwareness: HumanAwareness? = null
    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null

    private var humanInfoList: MutableList<HumanInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager
        recyclerview.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        humanInfoAdapter = HumanInfoAdapter()
        recyclerview.adapter = humanInfoAdapter

        // Find humans around when refresh button clicked.
        refresh_button.setOnClickListener {
            if (qiContext != null) {
                findHumansAround()
            }
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun getLayoutId(): Int = R.layout.activity_people_characteristics_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the provided QiContext.
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView!!.bindConversationTo(conversationStatus)

        val say = SayBuilder.with(qiContext)
                .withText("I can display characteristics about the human I'm seeing.")
                .build()

        say.run()

        // Get the HumanAwareness service from the QiContext.
        humanAwareness = qiContext.humanAwareness

        findHumansAround()
    }

    override fun onRobotFocusLost() {
        // Remove the QiContext.
        this.qiContext = null

        conversationBinder?.unbind()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun findHumansAround() {
        if (humanAwareness != null) {
            // Get the humans around the robot.
            val humansAroundFuture = humanAwareness?.async()?.humansAround

            humansAroundFuture?.andThenConsume { humansAround ->
                Log.i(TAG, humansAround.size.toString() + " human(s) around.")
                retrieveCharacteristics(humansAround)
            }
        }
    }

    private fun retrieveCharacteristics(humans: List<Human>) {
        // Get the Actuation service from the QiContext.
        val actuation = qiContext?.actuation

        // Get the robot frame.
        val robotFrame = actuation?.robotFrame()
        //we clear memory used for human who are being showed
        for (h in humanInfoList) {
            h.clearMemory()
        }

        humanInfoList = ArrayList()
        for (i in humans.indices) {
            // Get the human.
            val human = humans[i]

            // Get the characteristics.
            val age = human.estimatedAge.years
            val gender = human.estimatedGender
            val pleasureState = human.emotion.pleasure
            val excitementState = human.emotion.excitement
            val engagementIntentionState = human.engagementIntention
            val smileState = human.facialExpressions.smile
            val attentionState = human.attention
            val humanFrame = human.headFrame

            // Display the characteristics.
            Log.i(TAG, "----- Human $i -----")
            Log.i(TAG, "Age: $age year(s)")
            Log.i(TAG, "Gender: $gender")
            Log.i(TAG, "Pleasure state: $pleasureState")
            Log.i(TAG, "Excitement state: $excitementState")
            Log.i(TAG, "Engagement state: $engagementIntentionState")
            Log.i(TAG, "Smile state: $smileState")
            Log.i(TAG, "Attention state: $attentionState")

            // Compute the distance.
            val distance = robotFrame?.let { computeDistance(humanFrame, it) }
            // Display the distance between the human and the robot.
            Log.i(TAG, "Distance: $distance meter(s).")

            // Get face picture.
            val facePictureBuffer = human.facePicture.image.data
            facePictureBuffer.rewind()
            val pictureBufferSize = facePictureBuffer.remaining()
            val facePictureArray = ByteArray(pictureBufferSize)
            facePictureBuffer.get(facePictureArray)

            var facePicture: Bitmap? = null
            // Test if the robot has an empty picture (this can happen when he detects a human but not the face).
            if (pictureBufferSize != 0) {
                Log.i(TAG, "Picture available")
                facePicture = BitmapFactory.decodeByteArray(facePictureArray, 0, pictureBufferSize)
            } else {
                Log.i(TAG, "Picture not available")
            }

            val humanInfo = HumanInfo(age, gender, pleasureState, excitementState, engagementIntentionState, smileState, attentionState, distance!!, facePicture!!)
            humanInfoList.add(humanInfo)
        }

        displayHumanInfoList(humanInfoList)
    }

    private fun computeDistance(humanFrame: Frame, robotFrame: Frame): Double {
        // Get the TransformTime between the human frame and the robot frame.
        val transformTime = humanFrame.computeTransform(robotFrame)

        // Get the transform.
        val transform = transformTime.transform

        // Get the translation.
        val translation = transform.translation

        // Get the x and y components of the translation.
        val x = translation.x
        val y = translation.y

        // Compute the distance and return it.
        return sqrt(x * x + y * y)
    }

    private fun displayHumanInfoList(humanInfoList: List<HumanInfo>) {
        runOnUiThread { humanInfoAdapter.updateList(humanInfoList) }
    }
}
