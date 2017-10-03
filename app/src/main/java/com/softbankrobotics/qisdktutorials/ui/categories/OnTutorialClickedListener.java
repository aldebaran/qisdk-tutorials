package com.softbankrobotics.qisdktutorials.ui.categories;

import com.softbankrobotics.qisdktutorials.model.data.Tutorial;

/**
 * The listener used to handle a click on a tutorial.
 */
interface OnTutorialClickedListener {
    /**
     * Called when a tutorial is clicked.
     * @param tutorial the selected tutorial
     */
    void onTutorialClicked(Tutorial tutorial);
}
