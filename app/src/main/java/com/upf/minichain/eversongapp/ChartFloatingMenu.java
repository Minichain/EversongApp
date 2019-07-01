package com.upf.minichain.eversongapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.upf.minichain.eversongapp.chordChart.GuitarChordChart;
import com.upf.minichain.eversongapp.chordChart.PianoChordChart;
import com.upf.minichain.eversongapp.chordChart.StaffChordChart;
import com.upf.minichain.eversongapp.chordChart.UkuleleChordChart;
import com.upf.minichain.eversongapp.enums.ChartTab;

public class ChartFloatingMenu {
    Activity activity;
    Context context;
    FloatingActionMenu chartFloatingMenu;
    FloatingActionButton guitarFloatingButton;
    FloatingActionButton ukuleleFloatingButton;
    FloatingActionButton pianoFloatingButton;
    FloatingActionButton staffFloatingButton;
    FloatingActionButton chromaFloatingButton;
    int colorNormal;
    int colorPressed;

    public ChartFloatingMenu(Activity activity, Context context, int colorNormal, int colorPressed) {
        this.activity = activity;
        this.context = context;
        this.colorNormal = colorNormal;
        this.colorPressed = colorPressed;
    }

    public void createChartMenuFloatingMenu() {
        // Floating menu
        chartFloatingMenu = activity.findViewById(R.id.chart_menu_floating_menu);
        chartFloatingMenu.setClosedOnTouchOutside(true);
        chartFloatingMenu.setMenuButtonColorNormal(colorNormal);
        chartFloatingMenu.setMenuButtonColorPressed(colorPressed);
        chartFloatingMenu.setIconAnimated(false);

        setFloatingMenuIcon();

        // Floating buttons
        guitarFloatingButton = new FloatingActionButton(context);
        guitarFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        guitarFloatingButton.setColorNormal(colorNormal);
        guitarFloatingButton.setColorPressed(colorPressed);
        guitarFloatingButton.setImageResource(R.drawable.guitar_24dp);
        ukuleleFloatingButton = new FloatingActionButton(context);
        ukuleleFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        ukuleleFloatingButton.setColorNormal(colorNormal);
        ukuleleFloatingButton.setColorPressed(colorPressed);
        ukuleleFloatingButton.setImageResource(R.drawable.ukulele_24dp);
        pianoFloatingButton = new FloatingActionButton(context);
        pianoFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        pianoFloatingButton.setColorNormal(colorNormal);
        pianoFloatingButton.setColorPressed(colorPressed);
        pianoFloatingButton.setImageResource(R.drawable.piano_24dp);
        staffFloatingButton = new FloatingActionButton(context);
        staffFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        staffFloatingButton.setColorNormal(colorNormal);
        staffFloatingButton.setColorPressed(colorPressed);
        staffFloatingButton.setImageResource(R.drawable.g_clef_symbol_24dp);
        chromaFloatingButton = new FloatingActionButton(context);
        chromaFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chromaFloatingButton.setColorNormal(colorNormal);
        chromaFloatingButton.setColorPressed(colorPressed);
        chromaFloatingButton.setImageResource(R.drawable.bar_chart_24dp);

        updateFloatingMenu();

        chartFloatingMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
            }
        });

        guitarFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setChartTabSelected(ChartTab.GUITAR_TAB);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_guitar), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        ukuleleFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setChartTabSelected(ChartTab.UKULELE_TAB);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_ukulele), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        pianoFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setChartTabSelected(ChartTab.PIANO_TAB);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_piano), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        staffFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setChartTabSelected(ChartTab.STAFF_TAB);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_staff), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        chromaFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.setChartTabSelected(ChartTab.CHROMAGRAM);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_chromagram), Toast.LENGTH_SHORT);
                toast.show();
                onFloatingButtonPressed();
            }
        });

        setChordChart();
    }

    private void onFloatingButtonPressed() {
        setChordChart();
        setFloatingMenuIcon();
        chartFloatingMenu.toggle(true);
        updateFloatingMenu();
    }

    private void updateFloatingMenu() {
        chartFloatingMenu.removeAllMenuButtons();
        if (Parameters.getChartTabSelected() != ChartTab.GUITAR_TAB) {
            chartFloatingMenu.addMenuButton(guitarFloatingButton);
        }
        if (Parameters.getChartTabSelected() != ChartTab.UKULELE_TAB) {
            chartFloatingMenu.addMenuButton(ukuleleFloatingButton);
        }
        if (Parameters.getChartTabSelected() != ChartTab.PIANO_TAB) {
            chartFloatingMenu.addMenuButton(pianoFloatingButton);
        }
        if (Parameters.getChartTabSelected() != ChartTab.STAFF_TAB) {
            chartFloatingMenu.addMenuButton(staffFloatingButton);
        }
        if (Parameters.getChartTabSelected() != ChartTab.CHROMAGRAM) {
            chartFloatingMenu.addMenuButton(chromaFloatingButton);
        }
    }

    private void setFloatingMenuIcon() {
        Drawable menuDrawable = null;
        switch(Parameters.getChartTabSelected()) {
            case GUITAR_TAB:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.guitar_24dp);
                break;
            case UKULELE_TAB:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.ukulele_24dp);
                break;
            case PIANO_TAB:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.piano_24dp);
                break;
            case STAFF_TAB:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.g_clef_symbol_24dp);
                break;
            case CHROMAGRAM:
                menuDrawable = ContextCompat.getDrawable(activity, R.drawable.bar_chart_24dp);
                break;
        }
        if (menuDrawable != null) {
            chartFloatingMenu.getMenuIconView().setImageDrawable(menuDrawable);
        }
    }

    private void setChordChart() {
        switch(Parameters.getChartTabSelected()) {
            case GUITAR_TAB:
            default:
                UkuleleChordChart.hideChordChart(context);
                StaffChordChart.hideChordChart(context);
                PianoChordChart.hideChordChart(context);
                break;
            case UKULELE_TAB:
                GuitarChordChart.hideChordChart(context);
                StaffChordChart.hideChordChart(context);
                PianoChordChart.hideChordChart(context);
                break;
            case PIANO_TAB:
                GuitarChordChart.hideChordChart(context);
                UkuleleChordChart.hideChordChart(context);
                StaffChordChart.hideChordChart(context);
                break;
            case STAFF_TAB:
                GuitarChordChart.hideChordChart(context);
                UkuleleChordChart.hideChordChart(context);
                PianoChordChart.hideChordChart(context);
                break;
            case CHROMAGRAM:
                GuitarChordChart.hideChordChart(context);
                UkuleleChordChart.hideChordChart(context);
                StaffChordChart.hideChordChart(context);
                PianoChordChart.hideChordChart(context);
                break;
        }
    }
}
