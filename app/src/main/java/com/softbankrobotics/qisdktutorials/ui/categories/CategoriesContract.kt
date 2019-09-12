/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import android.content.Context

import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel

/**
 * Contract for the categories.
 */
internal interface CategoriesContract {

    interface View {
        fun showTutorials(tutorials: List<Tutorial>)
        fun selectTutorial(tutorial: Tutorial)
        fun goToTutorial(tutorial: Tutorial)
        fun selectCategory(category: TutorialCategory)
        fun selectLevel(level: TutorialLevel)
    }

    interface Presenter {
        fun bind(view: View)
        fun unbind()
        fun loadTutorials(category: TutorialCategory)
        fun loadTutorials(level: TutorialLevel)
        fun goToTutorialForQiChatbotId(tutorialQiChatbotId: String)
        fun goToTutorial(tutorial: Tutorial)
    }

    interface Robot {
        fun register(activity: CategoriesActivity)
        fun unregister(activity: CategoriesActivity)
        fun stopDiscussion(tutorial: Tutorial)
        fun selectTopic(category: TutorialCategory)
        fun selectLevel(level: TutorialLevel)
    }

    interface Router {
        fun goToTutorial(tutorial: Tutorial, context: Context)
    }
}
