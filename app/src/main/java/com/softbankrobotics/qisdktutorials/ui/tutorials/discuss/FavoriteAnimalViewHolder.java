package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;

/**
 * The view holder to show a favorite animal.
 */
class FavoriteAnimalViewHolder extends RecyclerView.ViewHolder {

    private TextView textView;
    private Button button;

    private OnFavoriteAnimalClickedListener onFavoriteAnimalClickedListener;

    FavoriteAnimalViewHolder(View itemView, OnFavoriteAnimalClickedListener onFavoriteAnimalClickedListener) {
        super(itemView);
        this.onFavoriteAnimalClickedListener = onFavoriteAnimalClickedListener;
        textView = itemView.findViewById(R.id.animal_name_textview);
        button = itemView.findViewById(R.id.delete_button);
    }

    /**
     * Binds a tutorial to the views.
     * @param animalName the animal name
     */
    void bind(final String animalName) {
        textView.setText(animalName);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFavoriteAnimalClickedListener != null) {
                    onFavoriteAnimalClickedListener.onAnimalRemoved(animalName);
                }
            }
        });
    }
}
