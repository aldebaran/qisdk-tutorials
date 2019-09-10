/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.motion.freeframes

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.GoToBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Actuation
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.actuation.FreeFrame
import com.aldebaran.qi.sdk.`object`.actuation.GoTo
import com.aldebaran.qi.sdk.`object`.actuation.Mapping
import com.aldebaran.qi.sdk.`object`.conversation.ConversationStatus
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationBinder
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity
import com.softbankrobotics.qisdktutorials.utils.KeyboardUtils
import kotlinx.android.synthetic.main.activity_go_to_world_tutorial.*

import java.util.ArrayList
import java.util.HashMap

private const val TAG = "GoToWorldActivity"

/**
 * The activity for the Go to world tutorial.
 */
class GoToWorldTutorialActivity : TutorialActivity(), RobotLifecycleCallbacks {

    private var conversationBinder: ConversationBinder? = null
    private var spinnerAdapter: ArrayAdapter<String>? = null

    // Store the selected location.
    private var selectedLocation: String? = null
    // Store the saved locations.
    private val savedLocations = HashMap<String, FreeFrame>()
    // The QiContext provided by the QiSDK.
    private var qiContext: QiContext? = null
    // Store the Actuation service.
    private var actuation: Actuation? = null
    // Store the Mapping service.
    private var mapping: Mapping? = null
    // Store the GoTo action.
    private var goTo: GoTo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSaveClick()
            }
            false
        }

        // Save location on save button clicked.
        save_button.setOnClickListener {handleSaveClick() }

        // Go to location on go to button clicked.
        goto_button.setOnClickListener {
            selectedLocation?.let {
                goto_button.isEnabled = false
                save_button.isEnabled = false
                goToLocation(it)
            }
        }

        // Store location on selection.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedLocation = parent.getItemAtPosition(position) as String
                Log.i(TAG, "onItemSelected: $selectedLocation")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLocation = null
                Log.i(TAG, "onNothingSelected")
            }
        }

        // Setup spinner adapter.
        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ArrayList())
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun getLayoutId() = R.layout.activity_go_to_world_tutorial

    override fun onRobotFocusGained(qiContext: QiContext) {
        Log.i(TAG, "Focus gained.")
        // Store the provided QiContext and services.
        this.qiContext = qiContext

        // Bind the conversational events to the view.
        val conversationStatus = qiContext.conversation.status(qiContext.robotContext)
        conversationBinder = conversationView.bindConversationTo(conversationStatus)

        actuation = qiContext.actuation
        mapping = qiContext.mapping

        val say = SayBuilder.with(qiContext)
                .withText("I can store locations and go to them.")
                .build()

        say.run()

        waitForInstructions()
    }

    override fun onRobotFocusLost() {
        Log.i(TAG, "Focus lost.")
        // Remove the QiContext.
        qiContext = null

        conversationBinder?.unbind()

        // Remove on started listeners from the GoTo action.
        goTo?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // Nothing here.
    }

    private fun handleSaveClick() {
        val location = editText.text.toString()
        editText.setText("")
        KeyboardUtils.hideKeyboard(this)
        // Save location only if new.
        if (location.isNotEmpty() && !savedLocations.containsKey(location)) {
            spinnerAdapter?.add(location)
            displayLine("Location added: $location", ConversationItemType.INFO_LOG)
            saveLocation(location)
        }
    }

    private fun waitForInstructions() {
        val message = "Waiting for instructions..."
        Log.i(TAG, message)
        displayLine(message, ConversationItemType.INFO_LOG)
        runOnUiThread {
            save_button.isEnabled = true
            goto_button.isEnabled = true
        }
    }

    private fun saveLocation(location: String) {
        // Get the robot frame asynchronously.
        val robotFrameFuture = actuation?.async()?.robotFrame()
        robotFrameFuture?.andThenConsume { robotFrame ->
            // Create a FreeFrame representing the current robot frame.
            val locationFrame = mapping?.makeFreeFrame()
            val transform = TransformBuilder.create().fromXTranslation(0.0)
            locationFrame?.update(robotFrame, transform, 0L)

            // Store the FreeFrame.
            if (locationFrame != null)
                savedLocations[location] = locationFrame
        }
    }

    private fun goToLocation(location: String) {
        // Get the FreeFrame from the saved locations.
        val freeFrame = savedLocations[location]

        // Extract the Frame asynchronously.
        val frameFuture = freeFrame?.async()?.frame()
        frameFuture?.andThenCompose { frame ->
            // Create a GoTo action.
            val goTo = GoToBuilder.with(qiContext)
                    .withFrame(frame)
                    .build()
                    .also { this.goTo = it }

            // Display text when the GoTo action starts.
            goTo.addOnStartedListener {
                val message = "Moving..."
                Log.i(TAG, message)
                displayLine(message, ConversationItemType.INFO_LOG)
            }
            this.goTo = goTo

            // Execute the GoTo action asynchronously.
            goTo.async().run()
        }?.thenConsume { future ->
            if (future.isSuccess) {
                Log.i(TAG, "Location reached: $location")
                waitForInstructions()
            } else if (future.hasError()) {
                Log.e(TAG, "Go to location error", future.error)
                waitForInstructions()
            }
        }
    }

    private fun displayLine(text: String, type: ConversationItemType) {
        runOnUiThread { conversationView.addLine(text, type) }
    }
}
