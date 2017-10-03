package com.softbankrobotics.qisdktutorials.ui.tutorials;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;
import com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar.TutorialToolbar;
import com.softbankrobotics.qisdktutorials.utils.Constants;

/**
 * Base class for a tutorial activity.
 */
public abstract class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setupToolbar();
    }

    /**
     * Provide the tutorial layout identifier.
     * @return The layout identifier.
     */
    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * Configures the toolbar.
     */
    private void setupToolbar() {
        TutorialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        int nameNotFound = -1;
        int nameResId = getIntent().getIntExtra(Constants.Intent.TUTORIAL_NAME_KEY, nameNotFound);
        TutorialLevel level = (TutorialLevel) getIntent().getSerializableExtra(Constants.Intent.TUTORIAL_LEVEL_KEY);
        if (nameResId != nameNotFound && level != null) {
            toolbar.setName(nameResId);
            toolbar.setLevel(level);
        }
    }
}
