/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.bilateralswitch

import android.content.Context
import android.support.constraint.ConstraintSet
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

import com.softbankrobotics.qisdktutorials.R
import kotlinx.android.synthetic.main.bilateral_switch.view.*

private const val BACKGROUND_FIRST_SECTION_COLOR = R.color.basic_green
private const val BACKGROUND_SECOND_SECTION_COLOR = R.color.advanced_orange

private const val TRANSITION_DURATION = 100

private const val FIRST_SECTION_TEXT = R.string.basic_level
private const val SECOND_SECTION_TEXT = R.string.advanced_level

class BilateralSwitch (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var allowClick = true
    private var isChecked = false
    private var shouldNotifyListener = true

    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var firstSectionName: String? = null
    private var secondSectionName: String? = null

    init {
        if (attrs != null) {
            getAttributes(context, attrs)
        }

        inflateLayout()
    }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet): this(context, attrs, 0)

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.BilateralSwitch, 0, 0)

        firstSectionName = typedArray.getString(R.styleable.BilateralSwitch_first_section_name)
        secondSectionName = typedArray.getString(R.styleable.BilateralSwitch_second_section_name)
    }

    private fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.bilateral_switch, this, true)

        setOnClickListener(this)

        levelView.setText(FIRST_SECTION_TEXT)
        color_layer.setBackgroundResource(BACKGROUND_FIRST_SECTION_COLOR)

        first_section?.text = firstSectionName
        second_section?.text = secondSectionName
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }


    override fun onClick(view: View) {
        if (!allowClick) {
            return
        }

        allowClick = false

        button_hover.visibility = View.VISIBLE
        collapseTransition()
    }

    fun setChecked(checked: Boolean) {
        if (isChecked != checked && allowClick) {
            shouldNotifyListener = false
            onClick(this)
        }
    }

    private fun collapseTransition() {
        val cs = ConstraintSet()
        cs.clone(layout)

        if (!isChecked) {
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.levelView, ConstraintSet.START)
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.levelView, ConstraintSet.END)
        } else {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.levelView, ConstraintSet.END)
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.levelView, ConstraintSet.START)
        }


        val firstTransition = AutoTransition()
        firstTransition.duration = TRANSITION_DURATION.toLong()

        firstTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}

            override fun onTransitionEnd(transition: Transition) {
                button_hover.visibility = View.GONE

                if (isChecked) {
                    levelView.setText(FIRST_SECTION_TEXT)
                } else {
                    levelView.setText(SECOND_SECTION_TEXT)
                }

                transition.removeListener(this)
                expandTransition()
            }

            override fun onTransitionCancel(transition: Transition) {}

            override fun onTransitionPause(transition: Transition) {}

            override fun onTransitionResume(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(
                this,
                firstTransition)

        cs.applyTo(layout)
    }

    private fun expandTransition() {
        val cs = ConstraintSet()
        cs.clone(layout)

        if (!isChecked) {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.second_section, ConstraintSet.END)
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.levelView, ConstraintSet.START)
            color_layer.setBackgroundResource(BACKGROUND_SECOND_SECTION_COLOR)
        } else {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.levelView, ConstraintSet.END)
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.first_section, ConstraintSet.START)
            color_layer.setBackgroundResource(BACKGROUND_FIRST_SECTION_COLOR)
        }

        val firstTransition = AutoTransition()
        firstTransition.duration = TRANSITION_DURATION.toLong()

        firstTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}

            override fun onTransitionEnd(transition: Transition) {
                isChecked = !isChecked
                transition.removeListener(this)
                allowClick = true

                if (onCheckedChangeListener != null && shouldNotifyListener) {
                    onCheckedChangeListener?.onCheckedChanged(isChecked)
                }

                shouldNotifyListener = true
            }

            override fun onTransitionCancel(transition: Transition) {}

            override fun onTransitionPause(transition: Transition) {}

            override fun onTransitionResume(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(
                this,
                firstTransition)

        cs.applyTo(layout)
    }

}
