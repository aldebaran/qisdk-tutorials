/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.qisdktutorials.ui.tutorials.perceptions.humanawareness

import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.softbankrobotics.qisdktutorials.R
import kotlinx.android.synthetic.main.human_info_layout.view.*

/**
 * The view holder to show human information.
 */
internal class HumanInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * Binds human information to the views.
     *
     * @param humanInfo the human information to bind
     */
    fun bind(humanInfo: HumanInfo) {
        val resources = itemView.resources
        itemView.age_textview.text = resources.getQuantityString(R.plurals.age, humanInfo.age, humanInfo.age)
        itemView.gender_textview.text = resources.getString(R.string.gender, humanInfo.gender)
        itemView.pleasure_state_textview.text = resources.getString(R.string.pleasure_state, humanInfo.pleasureState)
        itemView.excitement_state_textview.text = resources.getString(R.string.excitement_state, humanInfo.excitementState)
        itemView.engagement_intention_state_textview.text = resources.getString(R.string.engagement_intention_state, humanInfo.engagementIntentionState)
        itemView.smile_state_textview.text = resources.getString(R.string.smile_state, humanInfo.smileState)
        itemView.attention_state_textview.text = resources.getString(R.string.attention_state, humanInfo.attentionState)
        itemView.distance_textview.text = resources.getString(R.string.distance, humanInfo.distance)
        //we should put image bitmap to null to avoid setting image on recycled bitmap
        itemView.face_imageview.setImageBitmap(null)
        if (humanInfo.facePicture == null) {
            itemView.face_imageview.setBackgroundResource(R.drawable.ic_icons_cute_anon_unknown)
        } else {
            itemView.face_imageview.setImageBitmap(humanInfo.facePicture)
        }
    }
}
