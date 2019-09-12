/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness

import android.graphics.Bitmap

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.EngagementIntentionState
import com.aldebaran.qi.sdk.`object`.human.ExcitementState
import com.aldebaran.qi.sdk.`object`.human.Gender
import com.aldebaran.qi.sdk.`object`.human.PleasureState
import com.aldebaran.qi.sdk.`object`.human.SmileState

/**
 * Represents the human information.
 */
class HumanInfo(val age: Int, val gender: Gender, val pleasureState: PleasureState, val excitementState: ExcitementState, val engagementIntentionState: EngagementIntentionState, val smileState: SmileState, val attentionState: AttentionState, val distance: Double, facePicture: Bitmap) {
    var facePicture: Bitmap? = null
        private set

    init {
        this.facePicture = facePicture
    }

    /**
     * To clear the memory before setting a new bitmap
     * [https://developer.android.com/reference/android/graphics/Bitmap.html#recycle()](https://developer.android.com/reference/android/graphics/Bitmap.html#recycle())
     */
    fun clearMemory() {
        val localFacePicture = facePicture
        if (localFacePicture != null && !localFacePicture.isRecycled) {
            facePicture?.recycle()
            facePicture = null
        }
    }
}
