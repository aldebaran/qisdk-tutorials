package com.softbankrobotics.qisdktutorials.ui.tutorials.peoplecharacteristics;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.HumanInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter used to show humans information.
 */
class HumanInfoAdapter extends RecyclerView.Adapter<HumanInfoViewHolder> {

    private List<HumanInfo> humanInfoList = new ArrayList<>();

    @Override
    public HumanInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.human_info_layout, parent, false);
        return new HumanInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HumanInfoViewHolder holder, int position) {
        HumanInfo humanInfo = humanInfoList.get(position);
        holder.bind(humanInfo);
    }

    @Override
    public int getItemCount() {
        return humanInfoList.size();
    }

    void updateList(List<HumanInfo> humanInfoList) {
        this.humanInfoList = humanInfoList;
        notifyDataSetChanged();
    }
}
