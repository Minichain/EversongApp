package com.upf.minichain.eversongapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;

import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);

        checkNotationSetting();
        checkWindowingFunctionSetting();
    }

    private void checkNotationSetting() {
        RadioButton view;
        switch (Parameters.getInstance().getMusicalNotation()) {
            case ENGLISH_NOTATION:
                view = this.findViewById(R.id.english_notation);
                view.setChecked(true);
                break;
            case SOLFEGE_NOTATION:
            default:
                view = this.findViewById(R.id.solfege_notation);
                view.setChecked(true);
                break;
        }
    }

    public void onNotationSettingClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.english_notation:
                if (checked)
                    Parameters.getInstance().setMusicalNotation(MusicalNotationEnum.ENGLISH_NOTATION);
                    break;
            case R.id.solfege_notation:
            default:
                if (checked)
                    Parameters.getInstance().setMusicalNotation(MusicalNotationEnum.SOLFEGE_NOTATION);
                    break;
        }
    }

    private void checkWindowingFunctionSetting() {
        RadioButton view;
        switch(Parameters.getInstance().getWindowingFunction()) {
            case RECTANGULAR_WINDOW:
                view = this.findViewById(R.id.rectangular_window);
                view.setChecked(true);
                break;
            case HANNING_WINDOW:
                view = this.findViewById(R.id.hanning_window);
                view.setChecked(true);
                break;
            case HAMMING_WINDOW:
                view = this.findViewById(R.id.hamming_window);
                view.setChecked(true);
                break;
            case BLACKMAN_WINDOW:
            default:
                view = this.findViewById(R.id.blackman_window);
                view.setChecked(true);
                break;
        }

    }

    public void onWindowingSettingClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.rectangular_window:
                if (checked)
                    Parameters.getInstance().setWindowingFunction(WindowFunctionEnum.RECTANGULAR_WINDOW);
                break;
            case R.id.hanning_window:
                if (checked)
                    Parameters.getInstance().setWindowingFunction(WindowFunctionEnum.HANNING_WINDOW);
                break;
            case R.id.hamming_window:
                if (checked)
                    Parameters.getInstance().setWindowingFunction(WindowFunctionEnum.HAMMING_WINDOW);
                break;
            case R.id.blackman_window:
            default:
                if (checked)
                    Parameters.getInstance().setWindowingFunction(WindowFunctionEnum.BLACKMAN_WINDOW);
                break;
        }
    }
}
