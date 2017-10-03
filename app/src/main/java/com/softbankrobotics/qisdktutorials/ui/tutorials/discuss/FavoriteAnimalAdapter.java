package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softbankrobotics.qisdktutorials.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter used to show favorite animals.
 */
class FavoriteAnimalAdapter extends RecyclerView.Adapter<FavoriteAnimalViewHolder> {

    private List<String> favoriteAnimals;
    private OnFavoriteAnimalClickedListener onFavoriteAnimalClickedListener;

    FavoriteAnimalAdapter(OnFavoriteAnimalClickedListener onFavoriteAnimalClickedListener) {
        this.favoriteAnimals = new ArrayList<>();
        this.onFavoriteAnimalClickedListener = onFavoriteAnimalClickedListener;
    }

    @Override
    public FavoriteAnimalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_animal_layout, parent, false);
        return new FavoriteAnimalViewHolder(view, onFavoriteAnimalClickedListener);
    }

    @Override
    public void onBindViewHolder(FavoriteAnimalViewHolder holder, int position) {
        String animalName = favoriteAnimals.get(position);
        holder.bind(animalName);
    }

    @Override
    public int getItemCount() {
        return favoriteAnimals.size();
    }

    void addAnimal(String animalName) {
        favoriteAnimals.add(animalName);
        notifyItemInserted(favoriteAnimals.size() - 1);
    }

    void removeAnimal(String animalName) {
        int index = favoriteAnimals.indexOf(animalName);
        if (index != -1) {
            favoriteAnimals.remove(animalName);
            notifyItemRemoved(index);
        }
    }

    boolean containsAnimal(String animalName) {
        return favoriteAnimals.contains(animalName);
    }
}
