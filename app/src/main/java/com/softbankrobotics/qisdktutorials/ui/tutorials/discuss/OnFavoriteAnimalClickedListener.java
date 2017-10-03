package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

/**
 * The listener used to handle a click on a favorite animal.
 */
interface OnFavoriteAnimalClickedListener {
    /**
     * Called when a favorite animal is removed.
     * @param animalName the animal name
     */
    void onAnimalRemoved(String animalName);
}
