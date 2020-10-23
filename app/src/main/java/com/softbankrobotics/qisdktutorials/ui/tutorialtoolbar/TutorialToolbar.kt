/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
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
        back_arrow.setOnClickListener(listener)
    }

    fun setName(name: String) {
        title_textview.text = name
        invalidate()
        requestLayout()
    }

    fun setName(@StringRes resId: Int) {
        title_textview.text = resources.getString((resId))
        invalidate()
        requestLayout()
    }

    fun setLevel(level: TutorialLevel) {
        when (level) {
            TutorialLevel.BASIC -> {
                level_textview.setText(R.string.toolbar_basic_level)
                background_view.setBackgroundColor(ContextCompat.getColor(context, R.color.basic_green))
            }
            TutorialLevel.ADVANCED -> {
                level_textview.setText(R.string.toolbar_advanced_level)
                background_view.setBackgroundColor(ContextCompat.getColor(context, R.color.advanced_orange))
            }
        }

        invalidate()
        requestLayout()
    }
}
