/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.categories

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager

import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.Tutorial
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel
import kotlinx.android.synthetic.main.activity_categories.*

/**
 * The activity showing the tutorial categories.
 */
class CategoriesActivity : RobotActivity(), CategoriesContract.View, OnTutorialClickedListener {

    private lateinit var presenter: CategoriesContract.Presenter
    private lateinit var robot: CategoriesContract.Robot
    private lateinit var router: CategoriesContract.Router

    private var tutorialAdapter: TutorialAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        setupButtons()
        setupRecyclerView()
        setupSwitch()

        val presenter = CategoriesPresenter()
        robot = CategoriesRobot(presenter)
        router = CategoriesRouter()

        presenter.bind(this)
        robot.register(this)

        presenter.loadTutorials(TutorialCategory.TALK)
        this.presenter = presenter
    }

    override fun onResume() {
        super.onResume()
        tutorialAdapter?.unselectTutorials()
        tutorialAdapter?.setTutorialsEnabled(true)
    }

    override fun onDestroy() {
        robot.unregister(this)
        presenter.unbind()
        super.onDestroy()
    }

    override fun showTutorials(tutorials: List<Tutorial>) {
        runOnUiThread { tutorialAdapter?.updateTutorials(tutorials) }
    }

    override fun selectTutorial(tutorial: Tutorial) {
        runOnUiThread {
            tutorialAdapter?.selectTutorial(tutorial)
            tutorialAdapter?.setTutorialsEnabled(false)
        }
    }

    override fun goToTutorial(tutorial: Tutorial) {
        runOnUiThread { router.goToTutorial(tutorial, this@CategoriesActivity) }
    }

    override fun selectCategory(category: TutorialCategory) {
        runOnUiThread {
            when (category) {
                TutorialCategory.TALK -> talk_button.isChecked = true
                TutorialCategory.MOVE -> move_button.isChecked = true
                TutorialCategory.SMART -> smart_button.isChecked = true
            }
        }
    }

    override fun selectLevel(level: TutorialLevel) {
        runOnUiThread {
            when (level) {
                TutorialLevel.BASIC -> level_switch.setChecked(false)
                TutorialLevel.ADVANCED -> level_switch.setChecked(true)
            }
        }
    }

    override fun onTutorialClicked(tutorial: Tutorial) {
        tutorialAdapter?.selectTutorial(tutorial)
        tutorialAdapter?.setTutorialsEnabled(false)
        robot.stopDiscussion(tutorial)
    }

    /**
     * Configure the buttons.
     */
    private fun setupButtons() {
        talk_button.setOnClickListener {
            presenter.loadTutorials(TutorialCategory.TALK)
            robot.selectTopic(TutorialCategory.TALK)
        }

        move_button.setOnClickListener {
            presenter.loadTutorials(TutorialCategory.MOVE)
            robot.selectTopic(TutorialCategory.MOVE)
        }

        smart_button.setOnClickListener {
            presenter.loadTutorials(TutorialCategory.SMART)
            robot.selectTopic(TutorialCategory.SMART)
        }

        close_button.setOnClickListener { finishAffinity() }
    }

    /**
     * Configure the recycler view.
     */
    private fun setupRecyclerView() {
        tutorialAdapter = TutorialAdapter(this)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = tutorialAdapter

        val drawable = getDrawable(R.drawable.empty_divider_tutorials)
        if (drawable != null) {
            val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(drawable)
            recyclerview.addItemDecoration(dividerItemDecoration)
        }
    }

    /**
     * Configure the level switch.
     */
    private fun setupSwitch() {
        level_switch.setOnCheckedChangeListener { isChecked ->
            if (isChecked) {
                presenter.loadTutorials(TutorialLevel.ADVANCED)
                robot.selectLevel(TutorialLevel.ADVANCED)
            } else {
                presenter.loadTutorials(TutorialLevel.BASIC)
                robot.selectLevel(TutorialLevel.BASIC)
            }
        }
    }
}
