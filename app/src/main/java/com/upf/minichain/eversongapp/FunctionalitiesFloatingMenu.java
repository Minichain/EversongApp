package com.upf.minichain.eversongapp;

import android.content.Context;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.upf.minichain.eversongapp.enums.EversongFunctionalities;

public class FunctionalitiesFloatingMenu {
    EversongActivity activity;
    Context context;
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
        FloatingActionMenu functionalitiesFloatingMenu = activity.findViewById(R.id.functionalities_menu_floating_menu);
        functionalitiesFloatingMenu.setClosedOnTouchOutside(true);
        functionalitiesFloatingMenu.setMenuButtonColorNormal(colorNormal);
        functionalitiesFloatingMenu.setMenuButtonColorPressed(colorPressed);

        // Floating buttons
        FloatingActionButton chordDetectionFloatingButton = new FloatingActionButton(context);
        chordDetectionFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chordDetectionFloatingButton.setColorNormal(colorNormal);
        chordDetectionFloatingButton.setColorPressed(colorPressed);
        FloatingActionButton chordLibFloatingButton = new FloatingActionButton(context);
        chordLibFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chordLibFloatingButton.setColorNormal(colorNormal);
        chordLibFloatingButton.setColorPressed(colorPressed);
        FloatingActionButton tuningFloatingButton = new FloatingActionButton(context);
        tuningFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        tuningFloatingButton.setColorNormal(colorNormal);
        tuningFloatingButton.setColorPressed(colorPressed);

        functionalitiesFloatingMenu.addMenuButton(chordDetectionFloatingButton);
        functionalitiesFloatingMenu.addMenuButton(chordLibFloatingButton);
        functionalitiesFloatingMenu.addMenuButton(tuningFloatingButton);

        functionalitiesFloatingMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
            }
        });

        chordDetectionFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.CHORD_DETECTION);
                activity.setFunctionality();
            }
        });

        chordLibFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.CHORD_SCORE);
                activity.setFunctionality();
            }
        });

        tuningFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.TUNING);
                activity.setFunctionality();
            }
        });
    }
}
