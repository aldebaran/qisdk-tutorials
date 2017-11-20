package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter used to show greetings.
 */
class GreetingAdapter extends RecyclerView.Adapter<GreetingViewHolder> {

    private List<String> greetings;
    private OnGreetingRemovedListener onGreetingRemovedListener;

    GreetingAdapter(OnGreetingRemovedListener onGreetingRemovedListener) {
        this.greetings = new ArrayList<>();
        this.onGreetingRemovedListener = onGreetingRemovedListener;
    }

    @Override
    public GreetingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.greeting_layout, parent, false);
        return new GreetingViewHolder(view, onGreetingRemovedListener);
    }

    @Override
    public void onBindViewHolder(GreetingViewHolder holder, int position) {
        String greeting = greetings.get(position);
        holder.bind(greeting);
    }

    @Override
    public int getItemCount() {
        return greetings.size();
    }

    void addGreeting(String greeting) {
        greetings.add(greeting);
        notifyItemInserted(greetings.size() - 1);
    }

    void removeGreeting(String greeting) {
        int index = greetings.indexOf(greeting);
        if (index != -1) {
            greetings.remove(greeting);
            notifyItemRemoved(index);
        }
    }

    boolean containsGreeting(String greeting) {
        return greetings.contains(greeting);
    }
}
