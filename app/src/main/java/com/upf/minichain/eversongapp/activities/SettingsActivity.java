package com.upf.minichain.eversongapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.upf.minichain.eversongapp.BuildConfig;
import com.upf.minichain.eversongapp.Log;
import com.upf.minichain.eversongapp.Parameters;
import com.upf.minichain.eversongapp.R;
import com.upf.minichain.eversongapp.enums.ChordDetectionAlgorithm;
import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.l("SettingsActivityLog:: onCreating");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);

        setMusicalNotationSetting();
        setWindowingFunctionSetting();
        setChordBufferSizeSetting();
        setAudioBufferSizeSetting();
        setBandpassFilterLowFreqSetting();
        setBandpassFilterHighFreqSetting();
        setChordDetectionAlgorithmSetting();
        setPitchBufferSizeSetting();
        setDebugModeSetting();
        setChromagramNumHarmonicsSetting();
        setChromagramNumOctavesSetting();
        setChromagramNumBinsToSearchSetting();
    }

    private void setMusicalNotationSetting() {
        RadioButton view;
        switch (Parameters.getMusicalNotation()) {
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
                    Parameters.setMusicalNotation(MusicalNotationEnum.ENGLISH_NOTATION);
                    break;
            case R.id.solfege_notation:
            default:
                if (checked)
                    Parameters.setMusicalNotation(MusicalNotationEnum.SOLFEGE_NOTATION);
                    break;
        }
    }

    private void setWindowingFunctionSetting() {
        RadioButton view;
        switch(Parameters.getWindowingFunction()) {
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
                    Parameters.setWindowingFunction(WindowFunctionEnum.RECTANGULAR_WINDOW);
                break;
            case R.id.hanning_window:
                if (checked)
                    Parameters.setWindowingFunction(WindowFunctionEnum.HANNING_WINDOW);
                break;
            case R.id.hamming_window:
                if (checked)
                    Parameters.setWindowingFunction(WindowFunctionEnum.HAMMING_WINDOW);
                break;
            case R.id.blackman_window:
            default:
                if (checked)
                    Parameters.setWindowingFunction(WindowFunctionEnum.BLACKMAN_WINDOW);
                break;
        }
    }

    private void setChordBufferSizeSetting() {
        int bufferSize = Parameters.getChordBufferSize();
        SeekBar seekBar = this.findViewById(R.id.chord_buffer_size_seekbar);
        seekBar.setProgress(bufferSize);
        final TextView textView = this.findViewById(R.id.chord_buffer_size_seekbar_text);
        textView.setText(String.valueOf(bufferSize) + " chords");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress) + " chords");
                Parameters.setChordBufferSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setBandpassFilterLowFreqSetting() {
        int bufferSize = Parameters.BANDPASS_FILTER_LOW_FREQ;
        SeekBar seekBar = this.findViewById(R.id.bandpass_filter_low_freq_seekbar);
        seekBar.setProgress(bufferSize);
        final TextView textView = this.findViewById(R.id.bandpass_filter_low_freq_seekbar_text);
        textView.setText(String.valueOf(bufferSize) + " Hz");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress) + " Hz");
                Parameters.setBandpassFilterLowFreq(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setBandpassFilterHighFreqSetting() {
        final int minFreq = 1000;
        int bufferSize = Parameters.BANDPASS_FILTER_HIGH_FREQ;
        SeekBar seekBar = this.findViewById(R.id.bandpass_filter_high_freq_seekbar);
        seekBar.setProgress(bufferSize - minFreq);
        final TextView textView = this.findViewById(R.id.bandpass_filter_high_freq_seekbar_text);
        textView.setText(String.valueOf(bufferSize) + " Hz");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                seekBar.setProgress(progress);
                textView.setText(String.valueOf(progress + minFreq) + " Hz");
                Parameters.setBandpassFilterHighFreq(progress + minFreq);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setAudioBufferSizeSetting() {
        int seekBarProgress;
        switch(Parameters.BUFFER_SIZE) {
            case 2048:
                seekBarProgress = 0;
                break;
            case 4096:
                seekBarProgress = 1;
                break;
            case 8192:
                seekBarProgress = 2;
                break;
            case 16384:
            default:
                seekBarProgress = 3;
                break;
        }
        SeekBar seekBar = this.findViewById(R.id.audio_buffer_size_seekbar);
        seekBar.setProgress(seekBarProgress);
        final TextView textView = this.findViewById(R.id.audio_buffer_size_seekbar_text);
        textView.setText(String.valueOf((int)Math.pow(2, 11 + seekBarProgress)) + " s.");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int newAudioBufferSize = (int)Math.pow(2, 11 + progress);
                textView.setText(String.valueOf(newAudioBufferSize) + " s.");
                Parameters.setAudioBufferSize(newAudioBufferSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setChordDetectionAlgorithmSetting() {
        RadioButton view;
        switch (Parameters.getChordDetectionAlgorithm()) {
            case ADAM_STARK_ALGORITHM:
                view = this.findViewById(R.id.adam_stark_algorithm_option);
                view.setChecked(true);
                break;
            case EVERSONG_ALGORITHM_1:
                view = this.findViewById(R.id.eversong_algorithm_1_option);
                view.setChecked(true);
                break;
            case EVERSONG_ALGORITHM_2:
            default:
                view = this.findViewById(R.id.eversong_algorithm_2_option);
                view.setChecked(true);
                break;
        }
    }

    public void onChordDetectionAlgorithmSettingClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.adam_stark_algorithm_option:
                if (checked)
                    Parameters.setChordDetectionAlgorithm(ChordDetectionAlgorithm.ADAM_STARK_ALGORITHM);
                break;
            case R.id.eversong_algorithm_1_option:
                if (checked)
                    Parameters.setChordDetectionAlgorithm(ChordDetectionAlgorithm.EVERSONG_ALGORITHM_1);
                break;
            case R.id.eversong_algorithm_2_option:
            default:
                if (checked)
                    Parameters.setChordDetectionAlgorithm(ChordDetectionAlgorithm.EVERSONG_ALGORITHM_2);
                break;
        }
    }

    private void setPitchBufferSizeSetting() {
        int bufferSize = Parameters.getPitchBufferSize();
        SeekBar seekBar = this.findViewById(R.id.pitch_buffer_size_seekbar);
        seekBar.setProgress(bufferSize);
        final TextView textView = this.findViewById(R.id.pitch_buffer_size_seekbar_text);
        textView.setText(String.valueOf(bufferSize) + " values");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress) + " values");
                Parameters.setPitchBufferSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    private void setDebugModeSetting() {
        Switch switchView = this.findViewById(R.id.debug_mode_switch);
        if (!BuildConfig.FLAVOR.equals("dev")) {
            switchView.setVisibility(View.GONE);
            return;
        }
        switchView.setChecked(Parameters.isDebugMode());

        switchView.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String toastString = b ? getString(R.string.toast_debug_mode_enabled) : getString(R.string.toast_debug_mode_disabled);
                Toast toast = Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT);
                toast.show();
                Parameters.setDebugMode(b);
            }
        });
    }

    private void setChromagramNumHarmonicsSetting() {
        int seekBarProgress = Parameters.getChromagramNumHarmonics();
        SeekBar seekBar = this.findViewById(R.id.chromagram_num_harmonics_seekbar);
        seekBar.setProgress(seekBarProgress);
        final TextView textView = this.findViewById(R.id.chromagram_num_harmonics_seekbar_text);
        textView.setText(String.valueOf(seekBarProgress));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress));
                Parameters.setChromagramNumHarmonics(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setChromagramNumOctavesSetting() {
        int seekBarProgress = Parameters.getChromagramNumOctaves();
        SeekBar seekBar = this.findViewById(R.id.chromagram_num_octaves_seekbar);
        seekBar.setProgress(seekBarProgress);
        final TextView textView = this.findViewById(R.id.chromagram_num_octaves_seekbar_text);
        textView.setText(String.valueOf(seekBarProgress));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress));
                Parameters.setChromagramNumOctaves(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setChromagramNumBinsToSearchSetting() {
        int seekBarProgress = Parameters.getChromagramNumBinsToSearch();
        SeekBar seekBar = this.findViewById(R.id.chromagram_num_bins_seekbar);
        seekBar.setProgress(seekBarProgress);
        final TextView textView = this.findViewById(R.id.chromagram_num_bins_seekbar_text);
        textView.setText(String.valueOf(seekBarProgress));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int min = 1;
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                textView.setText(String.valueOf(progress));
                Parameters.setChromagramNumBinsToSearch(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
