package com.upf.minichain.eversongapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

        setFloatingMenuIcon();

        // Floating buttons
        FloatingActionButton guitarFloatingButton = new FloatingActionButton(context);
        guitarFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        guitarFloatingButton.setColorNormal(colorNormal);
        guitarFloatingButton.setColorPressed(colorPressed);
        guitarFloatingButton.setImageResource(R.drawable.guitar_24dp);
        FloatingActionButton ukuleleFloatingButton = new FloatingActionButton(context);
        ukuleleFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        ukuleleFloatingButton.setColorNormal(colorNormal);
        ukuleleFloatingButton.setColorPressed(colorPressed);
        ukuleleFloatingButton.setImageResource(R.drawable.ukulele_24dp);
        FloatingActionButton pianoFloatingButton = new FloatingActionButton(context);
        pianoFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        pianoFloatingButton.setColorNormal(colorNormal);
        pianoFloatingButton.setColorPressed(colorPressed);
        pianoFloatingButton.setImageResource(R.drawable.piano_24dp);
        FloatingActionButton staffFloatingButton = new FloatingActionButton(context);
        staffFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        staffFloatingButton.setColorNormal(colorNormal);
        staffFloatingButton.setColorPressed(colorPressed);
        staffFloatingButton.setImageResource(R.drawable.g_clef_symbol_24dp);
        FloatingActionButton chromaFloatingButton = new FloatingActionButton(context);
        chromaFloatingButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        chromaFloatingButton.setColorNormal(colorNormal);
        chromaFloatingButton.setColorPressed(colorPressed);
        chromaFloatingButton.setImageResource(R.drawable.bar_chart_24dp);

        chartFloatingMenu.addMenuButton(guitarFloatingButton);
        chartFloatingMenu.addMenuButton(ukuleleFloatingButton);
        chartFloatingMenu.addMenuButton(pianoFloatingButton);
        chartFloatingMenu.addMenuButton(staffFloatingButton);
        chartFloatingMenu.addMenuButton(chromaFloatingButton);

        chartFloatingMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
            }
        });

        guitarFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setChartTabSelected(ChartTab.GUITAR_TAB);
                setFloatingMenuIcon();
                setChordChart();
                chartFloatingMenu.toggle(true);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_guitar), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        ukuleleFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setChartTabSelected(ChartTab.UKULELE_TAB);
                setFloatingMenuIcon();
                setChordChart();
                chartFloatingMenu.toggle(true);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_ukulele), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        pianoFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setChartTabSelected(ChartTab.PIANO_TAB);
                setFloatingMenuIcon();
                setChordChart();
                chartFloatingMenu.toggle(true);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_piano), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        staffFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setChartTabSelected(ChartTab.STAFF_TAB);
                setFloatingMenuIcon();
                setChordChart();
                chartFloatingMenu.toggle(true);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_staff), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        chromaFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parameters.getInstance().setChartTabSelected(ChartTab.CHROMAGRAM);
                setFloatingMenuIcon();
                setChordChart();
                chartFloatingMenu.toggle(true);
                Toast toast = Toast.makeText(context, context.getString(R.string.toast_chart_menu_chromagram), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        setChordChart();
    }

    private void setFloatingMenuIcon() {
        Drawable menuDrawable = null;
        switch(Parameters.getInstance().getChartTabSelected()) {
            case GUITAR_TAB:
                menuDrawable = context.getDrawable(R.drawable.guitar_24dp);
                break;
            case UKULELE_TAB:
                menuDrawable = context.getDrawable(R.drawable.ukulele_24dp);
                break;
            case PIANO_TAB:
                menuDrawable = context.getDrawable(R.drawable.piano_24dp);
                break;
            case STAFF_TAB:
                menuDrawable = context.getDrawable(R.drawable.g_clef_symbol_24dp);
                break;
            case CHROMAGRAM:
                menuDrawable = context.getDrawable(R.drawable.bar_chart_24dp);
                break;
        }
        if (menuDrawable != null) {
            chartFloatingMenu.getMenuIconView().setImageDrawable(menuDrawable);
        }
    }

    private void setChordChart() {
        switch(Parameters.getInstance().getChartTabSelected()) {
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
