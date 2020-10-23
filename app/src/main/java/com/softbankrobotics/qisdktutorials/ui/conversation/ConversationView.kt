/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.conversation

import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

import com.aldebaran.qi.sdk.`object`.conversation.ConversationStatus
import com.softbankrobotics.qisdktutorials.R

class ConversationView constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private val adapter = ConversationAdapter()

    init {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null)

    fun bindConversationTo(conversationStatus: ConversationStatus): ConversationBinder = ConversationBinder.binding(conversationStatus, this)

    fun addLine(text: String, type: ConversationItemType) {
        adapter.addItem(text, type)
        scrollToPosition(adapter.itemCount - 1)
    }

    private fun setup() {
        layoutManager = LinearLayoutManager(context)
        setAdapter(adapter)

        val drawable = context.getDrawable(R.drawable.empty_divider_big)
        if (drawable != null) {
            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(drawable)
            this.addItemDecoration(dividerItemDecoration)
        }
    }
}
