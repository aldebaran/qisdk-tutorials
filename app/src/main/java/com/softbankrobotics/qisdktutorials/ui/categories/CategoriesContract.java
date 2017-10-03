package com.softbankrobotics.qisdktutorials.ui.categories;

import android.content.Context;

import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

import java.util.List;

/**
 * Contract for the categories.
 */
interface CategoriesContract {

    interface View {
        void showTutorials(List<Tutorial> tutorials);
        void selectTutorial(Tutorial tutorial);
        void goToTutorial(Tutorial tutorial);
        void selectCategory(TutorialCategory category);
        void selectLevel(TutorialLevel level);
    }

    interface Presenter {
        void bind(View view);
        void unbind();
        void loadTutorials(TutorialCategory category);
        void loadTutorials(TutorialLevel level);
        void goToTutorialForDiscussId(String tutorialDiscussId);
        void goToTutorial(Tutorial tutorial);
    }

    interface Robot {
        void register(CategoriesActivity activity);
        void unregister(CategoriesActivity activity);
        void stopDiscussion(Tutorial tutorial);
        void selectTopic(TutorialCategory category);
        void selectLevel(TutorialLevel level);
    }

    interface Router {
        void goToTutorial(Tutorial tutorial, Context context);
    }
}
