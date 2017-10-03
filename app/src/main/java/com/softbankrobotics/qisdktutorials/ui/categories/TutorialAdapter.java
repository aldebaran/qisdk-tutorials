package com.softbankrobotics.qisdktutorials.ui.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter used to show tutorials.
 */
class TutorialAdapter extends RecyclerView.Adapter<TutorialViewHolder> {

    private List<Tutorial> tutorials;
    private OnTutorialClickedListener onTutorialClickedListener;

    TutorialAdapter(OnTutorialClickedListener onTutorialClickedListener) {
        this.tutorials = new ArrayList<>();
        this.onTutorialClickedListener = onTutorialClickedListener;
    }

    @Override
    public TutorialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_layout, parent, false);
        return new TutorialViewHolder(view, onTutorialClickedListener);
    }

    @Override
    public void onBindViewHolder(TutorialViewHolder holder, int position) {
        Tutorial tutorial = tutorials.get(position);
        holder.bind(tutorial);
    }

    @Override
    public int getItemCount() {
        return tutorials.size();
    }

    /**
     * Update the tutorials list.
     * @param tutorials the tutorials
     */
    void updateTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
        notifyDataSetChanged();
    }

    /**
     * Select the specified tutorial.
     * @param tutorial the tutorial
     */
    void selectTutorial(Tutorial tutorial) {
        tutorial.setSelected(true);
        notifyDataSetChanged();
    }

    /**
     * Unselect all the tutorials.
     */
    void unselectTutorials() {
        for (Tutorial tutorial : tutorials) {
            tutorial.setSelected(false);
        }
        notifyDataSetChanged();
    }

    /**
     * Enable / disable all the tutorials.
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    void setTutorialsEnabled(boolean enabled) {
        for (Tutorial tutorial : tutorials) {
            tutorial.setEnabled(enabled);
        }
        notifyDataSetChanged();
    }
}
