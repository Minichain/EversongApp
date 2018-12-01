package com.upf.minichain.eversongapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;

import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);

        checkNotationSetting();
    }

    private void checkNotationSetting() {
        if (Parameters.getInstance().getMusicalNotation() == MusicalNotationEnum.ENGLISH_NOTATION) {
            RadioButton view;
            view = this.findViewById(R.id.english_notation);
            view.setChecked(true);
        } else if (Parameters.getInstance().getMusicalNotation() == MusicalNotationEnum.SOLFEGE_NOTATION) {
            RadioButton view;
            view = this.findViewById(R.id.solfege_notation);
            view.setChecked(true);
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
}
