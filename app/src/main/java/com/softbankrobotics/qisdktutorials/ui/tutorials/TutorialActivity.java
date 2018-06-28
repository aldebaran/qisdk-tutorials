package com.softbankrobotics.qisdktutorials.ui.tutorials;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;

import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;
import com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar.TutorialToolbar;
import com.softbankrobotics.qisdktutorials.utils.Constants;

/**
 * Base class for a tutorial activity.
 */
public abstract class TutorialActivity extends RobotActivity {

    private View rootView;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        rootView = findViewById(android.R.id.content);
        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                // Hide system UI if keyboard is closed.
                if (keypadHeight <= screenHeight * 0.30) {
                    hideSystemUI();
                }
            }
        };

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    @Override
    protected void onPause() {
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        super.onPause();
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

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
