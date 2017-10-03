package com.softbankrobotics.qisdktutorials.ui.bilateralswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.softbankrobotics.qisdktutorials.R;

public class BilateralSwitch extends FrameLayout implements View.OnClickListener {

    private final static int BASIC_TEXT_COLOR = Color.GRAY;
    private final static int HOVERED_TEXT_COLOR = Color.WHITE;

    private final static int BACKGROUND_FIRST_SECTION_COLOR = R.color.basic_green;
    private final static int BACKGROUND_SECOND_SECTION_COLOR = R.color.advanced_orange;

    private final static int TRANSITION_DURATION = 100;

    private final static int FIRST_SECTION_IMAGE = R.drawable.ic_img_btn_statusgreen;
    private final static int SECOND_SECTION_IMAGE = R.drawable.ic_img_btn_statusorange;

    ConstraintLayout constraintLayout;
    ImageView button;
    View buttonHover;
    View colorLayer;

    private boolean allowClick = true;
    private boolean isChecked = false;
    private boolean shouldNotifyListener = true;
    private TextView firstSection;
    private TextView secondSection;

    private OnCheckedChangeListener onCheckedChangeListener;
    private String firstSectionName;
    private String secondSectionName;

    public BilateralSwitch(Context context) {
        this(context, null);
    }

    public BilateralSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BilateralSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            getAttributes(context, attrs);
        }

        inflateLayout();
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BilateralSwitch, 0, 0);

        firstSectionName = typedArray.getString(R.styleable.BilateralSwitch_first_section_name);
        secondSectionName = typedArray.getString(R.styleable.BilateralSwitch_second_section_name);
    }

    private void inflateLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.bilateral_switch, this, true);

        constraintLayout = findViewById(R.id.layout);

        button = findViewById(R.id.button);
        colorLayer = findViewById(R.id.color_layer);
        firstSection = findViewById(R.id.first_section);
        secondSection = findViewById(R.id.second_section);
        buttonHover = findViewById(R.id.button_hover);

        setOnClickListener(this);

        button.setImageResource(FIRST_SECTION_IMAGE);
        secondSection.setTextColor(BASIC_TEXT_COLOR);
        colorLayer.setBackgroundResource(BACKGROUND_FIRST_SECTION_COLOR);
        firstSection.setTextColor(HOVERED_TEXT_COLOR);

        if (firstSectionName != null) {
            firstSection.setText(firstSectionName);
        }

        if (secondSectionName != null) {
            secondSection.setText(secondSectionName);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }


    @Override
    public void onClick(View view) {
        if (!allowClick) {
            return;
        }

        allowClick = false;

        buttonHover.setVisibility(View.VISIBLE);
        collapseTransition();
    }

    public void setChecked(boolean checked) {
        if (isChecked != checked && allowClick) {
            shouldNotifyListener = false;
            onClick(this);
        }
    }

    private void collapseTransition() {
        ConstraintSet cs = new ConstraintSet();
        cs.clone(constraintLayout);

        if (!isChecked) {
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.button, ConstraintSet.START);
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.button, ConstraintSet.END);
        } else {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.button, ConstraintSet.END);
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.button, ConstraintSet.START);
        }


        final AutoTransition firstTransition = new AutoTransition();
        firstTransition.setDuration(TRANSITION_DURATION);

        firstTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                buttonHover.setVisibility(GONE);

                if (isChecked) {
                    firstSection.setTextColor(HOVERED_TEXT_COLOR);
                    secondSection.setTextColor(BASIC_TEXT_COLOR);
                    button.setImageResource(FIRST_SECTION_IMAGE);
                } else {
                    firstSection.setTextColor(BASIC_TEXT_COLOR);
                    secondSection.setTextColor(HOVERED_TEXT_COLOR);
                    button.setImageResource(SECOND_SECTION_IMAGE);
                }

                transition.removeListener(this);
                expandTransition();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(
                this,
                firstTransition);

        cs.applyTo(constraintLayout);
    }

    private void expandTransition() {
        ConstraintSet cs = new ConstraintSet();
        cs.clone(constraintLayout);

        if (!isChecked) {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.second_section, ConstraintSet.END);
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.button, ConstraintSet.START);
            colorLayer.setBackgroundResource(BACKGROUND_SECOND_SECTION_COLOR);
        } else {
            cs.connect(R.id.color_layer, ConstraintSet.END, R.id.button, ConstraintSet.END);
            cs.connect(R.id.color_layer, ConstraintSet.START, R.id.first_section, ConstraintSet.START);
            colorLayer.setBackgroundResource(BACKGROUND_FIRST_SECTION_COLOR);
        }

        AutoTransition firstTransition = new AutoTransition();
        firstTransition.setDuration(TRANSITION_DURATION);

        firstTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                isChecked = !isChecked;
                transition.removeListener(this);
                allowClick = true;

                if (onCheckedChangeListener != null && shouldNotifyListener) {
                    onCheckedChangeListener.onCheckedChanged(isChecked);
                }

                shouldNotifyListener = true;
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(
                this,
                firstTransition);

        cs.applyTo(constraintLayout);
    }
}
