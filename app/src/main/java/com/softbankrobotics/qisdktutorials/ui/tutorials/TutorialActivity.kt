/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials

import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewTreeObserver

import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel
import com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar.TutorialToolbar
import com.softbankrobotics.qisdktutorials.utils.Constants
import kotlinx.android.synthetic.main.tutorial_toolbar.*

/**
 * Base class for a tutorial activity.
 */
abstract class TutorialActivity : RobotActivity() {

    private lateinit var rootView: View
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    /**
     * Provide the tutorial layout identifier.
     * @return The layout identifier.
     */
    @get:LayoutRes
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setupToolbar()
    }

    override fun onResume() {
        super.onResume()

        rootView = findViewById(android.R.id.content)
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight.minus(rect.bottom)

            // Hide system UI if keyboard is closed.
            if (keypadHeight <= screenHeight * 0.30) {
                hideSystemUI()
            }
        }

        rootView.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)
    }

    override fun onPause() {
        rootView.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        super.onPause()
    }

    /**
     * Configures the toolbar.
     */
    private fun setupToolbar() {
        val toolbar = findViewById<TutorialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val nameNotFound = -1
        val nameResId = intent.getIntExtra(Constants.Intent.TUTORIAL_NAME_KEY, nameNotFound)
        val level = intent.getSerializableExtra(Constants.Intent.TUTORIAL_LEVEL_KEY) as TutorialLevel
        if (nameResId != nameNotFound) {
            toolbar.setName(nameResId)
            toolbar.setLevel(level)
        }

        close_button.setOnClickListener { finishAffinity() }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
