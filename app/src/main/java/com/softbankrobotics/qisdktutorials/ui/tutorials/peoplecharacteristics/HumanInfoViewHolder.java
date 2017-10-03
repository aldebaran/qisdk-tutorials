package com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.HumanInfo;

/**
 * The view holder to show human information.
 */
class HumanInfoViewHolder extends RecyclerView.ViewHolder {

    private TextView ageTextView;
    private TextView genderTextView;
    private TextView pleasureStateTextView;
    private TextView excitementStateTextView;
    private TextView smileStateTextView;
    private TextView attentionStateTextView;
    private TextView distanceTextView;

    HumanInfoViewHolder(View itemView) {
        super(itemView);
        ageTextView = itemView.findViewById(R.id.age_textview);
        genderTextView = itemView.findViewById(R.id.gender_textview);
        pleasureStateTextView = itemView.findViewById(R.id.pleasure_state_textview);
        excitementStateTextView = itemView.findViewById(R.id.excitement_state_textview);
        smileStateTextView = itemView.findViewById(R.id.smile_state_textview);
        attentionStateTextView = itemView.findViewById(R.id.attention_state_textview);
        distanceTextView = itemView.findViewById(R.id.distance_textview);
    }

    /**
     * Binds human information to the views.
     * @param humanInfo the human information to bind
     */
    void bind(final HumanInfo humanInfo) {
        Resources resources = itemView.getResources();
        ageTextView.setText(resources.getQuantityString(R.plurals.age, humanInfo.getAge(), humanInfo.getAge()));
        genderTextView.setText(resources.getString(R.string.gender, humanInfo.getGender()));
        pleasureStateTextView.setText(resources.getString(R.string.pleasure_state, humanInfo.getPleasureState()));
        excitementStateTextView.setText(resources.getString(R.string.excitement_state, humanInfo.getExcitementState()));
        smileStateTextView.setText(resources.getString(R.string.smile_state, humanInfo.getSmileState()));
        attentionStateTextView.setText(resources.getString(R.string.attention_state, humanInfo.getAttentionState()));
        distanceTextView.setText(resources.getString(R.string.distance, humanInfo.getDistance()));
    }
}
