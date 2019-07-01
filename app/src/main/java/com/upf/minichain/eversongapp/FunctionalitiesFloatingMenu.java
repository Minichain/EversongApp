package com.upf.minichain.eversongapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.upf.minichain.eversongapp.enums.EversongFunctionalities;

public class FunctionalitiesFloatingMenu {
    EversongActivity activity;
    Context context;
    FloatingActionMenu functionalitiesFloatingMenu;
    FloatingActionButton chordDetectionFloatingButton;
    FloatingActionButton chordLibFloatingButton;
    FloatingActionButton tuningFloatingButton;
    int colorNormal;
    int colorPressed;

    public FunctionalitiesFloatingMenu(EversongActivity activity, Context context, int colorNormal, int colorPressed) {
        this.activity = activity;
        this.context = context;
        this.colorNormal = colorNormal;
        this.colorPressed = colorPressed;
    }

    public void createChartMenuFloatingMenu() {
        // Floating menu
        functionalitiesFloatingMenu = activity.findViewById(R.id.functionalities_menu_floating_menu);
        functionalitiesFloatingMenu.setClosedOnTouchOutside(true);
        functionalitiesFloatingMenu.setMenuButtonColorNormal(colorNormal);
        functionalitiesFloatingMenu.setMenuButtonColorPressed(colorPressed);
        functionalitiesFloatingMenu.setIconAnimated(false);

        setFloatingMenuIcon();

        // Floating buttons
        chordDetectionFloatingButton = new FloatingActionButton(context);
        chordDetectionFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chordDetectionFloatingButton.setColorNormal(colorNormal);
        chordDetectionFloatingButton.setColorPressed(colorPressed);
        chordDetectionFloatingButton.setImageResource(R.drawable.ear_24dp);
        chordLibFloatingButton = new FloatingActionButton(context);
        chordLibFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chordLibFloatingButton.setColorNormal(colorNormal);
        chordLibFloatingButton.setColorPressed(colorPressed);
        chordLibFloatingButton.setImageResource(R.drawable.open_book_24dp);
        tuningFloatingButton = new FloatingActionButton(context);
        tuningFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        tuningFloatingButton.setColorNormal(colorNormal);
        tuningFloatingButton.setColorPressed(colorPressed);
        tuningFloatingButton.setImageResource(R.drawable.tuner_24dp);

        updateFloatingMenu();

        functionalitiesFloatingMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
            }
        });

        chordDetectionFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setFunctionalitySelected(EversongFunctionalities.CHORD_DETECTION);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_functionalities_menu_chord_detection), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        chordLibFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setFunctionalitySelected(EversongFunctionalities.CHORD_SCORE);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_functionalities_menu_chord_library), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        tuningFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setFunctionalitySelected(EversongFunctionalities.TUNING);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_functionalities_menu_tuner), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });
    }

    private void onFloatingButtonPressed() {
        activity.setFunctionality();
        setFloatingMenuIcon();
        functionalitiesFloatingMenu.toggle(true);
        updateFloatingMenu();
    }

    private void updateFloatingMenu() {
        functionalitiesFloatingMenu.removeAllMenuButtons();
        if (Parameters.getFunctionalitySelected() != EversongFunctionalities.CHORD_DETECTION) {
            functionalitiesFloatingMenu.addMenuButton(chordDetectionFloatingButton);
        }
        if (Parameters.getFunctionalitySelected() != EversongFunctionalities.CHORD_SCORE) {
            functionalitiesFloatingMenu.addMenuButton(chordLibFloatingButton);
        }
        if (Parameters.getFunctionalitySelected() != EversongFunctionalities.TUNING) {
            functionalitiesFloatingMenu.addMenuButton(tuningFloatingButton);
        }
    }

    private void setFloatingMenuIcon() {
        Drawable menuDrawable = null;
        switch(Parameters.getFunctionalitySelected()) {
            case CHORD_DETECTION:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.ear_24dp);
                break;
            case CHORD_SCORE:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.open_book_24dp);
                break;
            case TUNING:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.tuner_24dp);
                break;
        }
        if (menuDrawable != null) {
            functionalitiesFloatingMenu.getMenuIconView().setImageDrawable(menuDrawable);
        }
    }
}
