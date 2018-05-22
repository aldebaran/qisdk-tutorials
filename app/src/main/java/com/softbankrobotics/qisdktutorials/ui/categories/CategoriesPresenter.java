package com.softbankrobotics.qisdktutorials.ui.categories;

import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;
import com.softbankrobotics.qisdktutorials.model.repository.TutorialRepository;

import java.util.List;

/**
 * The presenter for the tutorial categories.
 */
class CategoriesPresenter implements CategoriesContract.Presenter {
    private CategoriesContract.View view;
    private final TutorialRepository tutorialRepository;
    private List<Tutorial> loadedTutorials;
    private TutorialCategory selectedCategory = TutorialCategory.TALK;
    private TutorialLevel selectedLevel = TutorialLevel.BASICS;

    CategoriesPresenter() {
        this.tutorialRepository = new TutorialRepository();
    }

    @Override
    public void bind(CategoriesContract.View view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        this.view = null;
    }

    @Override
    public void loadTutorials(TutorialCategory category) {
        selectedCategory = category;
        updateTutorials();
    }

    @Override
    public void loadTutorials(TutorialLevel level) {
        selectedLevel = level;
        updateTutorials();
    }

    @Override
    public void goToTutorialForQiChatbotId(String tutorialQiChatbotId) {
        for (Tutorial tutorial : loadedTutorials) {
            if (tutorial.getQiChatbotId().equals(tutorialQiChatbotId)) {
                view.selectTutorial(tutorial);
                view.goToTutorial(tutorial);
                break;
            }
        }
    }

    @Override
    public void goToTutorial(Tutorial tutorial) {
        view.goToTutorial(tutorial);
    }

    private void updateTutorials() {
        loadedTutorials = tutorialRepository.getTutorials(selectedCategory, selectedLevel);
        view.selectCategory(selectedCategory);
        view.selectLevel(selectedLevel);
        view.showTutorials(loadedTutorials);
    }
}
