package com.upf.minichain.eversongapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class ChordPlayer {
    private SoundPool soundPool;
    private Integer[] soundPoolChord;
    private boolean soundPoolLoaded;
    private AudioManager audioManager;
    private Context context;

    public ChordPlayer(Context context) {
        this.context = context;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        loadSoundPool();
    }

    private void loadSoundPool() {
        soundPoolChord = new Integer[NotesEnum.numberOfNotes];
        soundPoolChord[0] = soundPool.load(context, R.raw.piano_a_3, 1);
        soundPoolChord[1] = soundPool.load(context, R.raw.piano_a_sharp_3, 1);
        soundPoolChord[2] = soundPool.load(context, R.raw.piano_b_3, 1);
        soundPoolChord[3] = soundPool.load(context, R.raw.piano_c_4, 1);
        soundPoolChord[4] = soundPool.load(context, R.raw.piano_c_sharp_4, 1);
        soundPoolChord[5] = soundPool.load(context, R.raw.piano_d_4, 1);
        soundPoolChord[6] = soundPool.load(context, R.raw.piano_d_sharp_4, 1);
        soundPoolChord[7] = soundPool.load(context, R.raw.piano_e_4, 1);
        soundPoolChord[8] = soundPool.load(context, R.raw.piano_f_4, 1);
        soundPoolChord[9] = soundPool.load(context, R.raw.piano_f_sharp_4, 1);
        soundPoolChord[10] = soundPool.load(context, R.raw.piano_g_4, 1);
        soundPoolChord[11] = soundPool.load(context, R.raw.piano_g_sharp_4, 1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;
            }
        });
    }

    public void playChord(NotesEnum tonic, ChordTypeEnum chordType) {
        if (!soundPoolLoaded
                || tonic == null || tonic.getValue() == -1
                || chordType == null || chordType.getValue() == -1) {
            return;
        }
        NotesEnum[] chordNotes = Utils.getChordNotes(tonic, chordType);
        if (chordNotes[2] == NotesEnum.NO_NOTE) {
            float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 2;
            soundPool.play(soundPoolChord[chordNotes[0].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[1].getValue()], streamVolume, streamVolume, 1, 0, 1f);
        } else if (chordNotes[3] == NotesEnum.NO_NOTE) {
            float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 3;
            soundPool.play(soundPoolChord[chordNotes[0].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[1].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[2].getValue()], streamVolume, streamVolume, 1, 0, 1f);
        } else {
            float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 4;
            soundPool.play(soundPoolChord[chordNotes[0].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[1].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[2].getValue()], streamVolume, streamVolume, 1, 0, 1f);
            soundPool.play(soundPoolChord[chordNotes[3].getValue()], streamVolume, streamVolume, 1, 0, 1f);
        }
    }
}
