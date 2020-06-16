/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.splashscreen

import android.content.Intent
import android.os.Bundle

import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.ui.categories.CategoriesActivity

import java.util.Timer
import java.util.TimerTask

/**
 * The splashscreen activity.
 */
class SplashScreenActivity : RobotActivity() {

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onResume() {
        super.onResume()

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                goToCategories()
            }
        }, 1500)
    }

    override fun onPause() {
        timer?.cancel()
        super.onPause()
    }

    private fun goToCategories() {
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)

        finish()
    }
}
