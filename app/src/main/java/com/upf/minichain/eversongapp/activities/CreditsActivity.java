package com.upf.minichain.eversongapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.upf.minichain.eversongapp.Log;
import com.upf.minichain.eversongapp.R;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.l("SettingsActivityLog:: onCreating");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits_activity);
    }
}
