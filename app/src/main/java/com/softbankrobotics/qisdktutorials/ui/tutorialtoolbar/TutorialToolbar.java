package com.softbankrobotics.qisdktutorials.ui.tutorialtoolbar;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;

public class TutorialToolbar extends Toolbar {

    private ImageView backArrow;
    private TextView titleTextView;
    private TextView levelTextView;
    private View backgroundView;

    public TutorialToolbar(Context context) {
        this(context, null);
    }

    public TutorialToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TutorialToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setNavigationOnClickListener(OnClickListener listener) {
        backArrow.setOnClickListener(listener);
    }

    public void setName(String name) {
        titleTextView.setText(name);
        invalidate();
        requestLayout();
    }

    public void setName(@StringRes int resId) {
        titleTextView.setText(resId);
        invalidate();
        requestLayout();
    }

    public void setLevel(TutorialLevel level) {
        switch (level) {
            case BASICS:
                levelTextView.setText(R.string.toolbar_basic_level);
                backgroundView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.basic_green));
                break;
            case ADVANCED:
                levelTextView.setText(R.string.toolbar_advanced_level);
                backgroundView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.advanced_orange));
                break;
            default:
                throw new IllegalArgumentException("Unknown tutorial level: " + level);
        }

        invalidate();
        requestLayout();
    }

    private void init() {
        inflate(getContext(), R.layout.tutorial_toolbar, this);
        backArrow = findViewById(R.id.backArrow);
        titleTextView = findViewById(R.id.titleTextView);
        levelTextView = findViewById(R.id.levelTextView);
        backgroundView = findViewById(R.id.backgroundView);
    }
}
