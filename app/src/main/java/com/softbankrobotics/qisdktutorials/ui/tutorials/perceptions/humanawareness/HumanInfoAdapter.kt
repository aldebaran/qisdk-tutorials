/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.softbankrobotics.qisdktutorials.R

import java.util.ArrayList

/**
 * The adapter used to show humans information.
 */
internal class HumanInfoAdapter : RecyclerView.Adapter<HumanInfoViewHolder>() {

    private var humanInfoList: List<HumanInfo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HumanInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.human_info_layout, parent, false)
        return HumanInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HumanInfoViewHolder, position: Int) {
        val humanInfo = humanInfoList[position]
        holder.bind(humanInfo)
    }

    override fun getItemCount() = humanInfoList.size

    fun updateList(humanInfoList: List<HumanInfo>) {
        this.humanInfoList = humanInfoList
        notifyDataSetChanged()
    }
}
