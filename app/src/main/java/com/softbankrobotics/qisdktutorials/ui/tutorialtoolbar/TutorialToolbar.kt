/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View

import com.softbankrobotics.qisdktutorials.R
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel
import kotlinx.android.synthetic.main.tutorial_toolbar.view.*

class TutorialToolbar (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : Toolbar(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.tutorial_toolbar, this)
    }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs,0)

    override fun setNavigationOnClickListener(listener: OnClickListener) {
        backArrow.setOnClickListener(listener)
    }

    fun setName(name: String) {
        titleTextView.text = name
        invalidate()
        requestLayout()
    }

    fun setName(@StringRes resId: Int) {
        titleTextView.setText(resId)
        invalidate()
        requestLayout()
    }

    fun setLevel(level: TutorialLevel) {
        when (level) {
            TutorialLevel.BASIC -> {
                levelTextView.setText(R.string.toolbar_basic_level)
                backgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.basic_green))
            }
            TutorialLevel.ADVANCED -> {
                levelTextView.setText(R.string.toolbar_advanced_level)
                backgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.advanced_orange))
            }
        }

        invalidate()
        requestLayout()
    }
}
