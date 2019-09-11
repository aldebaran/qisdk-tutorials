/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.model.data

import android.support.annotation.StringRes

/**
 * Represents a tutorial.
 */
class Tutorial(val id: TutorialId, @param:StringRes @field:StringRes @get:StringRes val nameResId: Int, val qiChatbotId: String, val tutorialLevel: TutorialLevel) {
    var isSelected: Boolean = false
    var isEnabled: Boolean = true
}
