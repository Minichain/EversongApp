package com.upf.minichain.eversongapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.upf.minichain.eversongapp.enums.BroadcastMessage;

public class EversongService extends Service {
    EversongServiceBroadcastReceiver eversongBroadCastReceiver;

    @Override
    public void onCreate() {
        Log.l("EversongServiceLog:: onCreate service");
        eversongBroadCastReceiver = new EversongServiceBroadcastReceiver();
        registerEversongServiceBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.l("EversongServiceLog:: onStartCommand service");
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.l("EversongServiceLog:: onBind service");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.l("EversongServiceLog:: onDestroy service");
    }

    private void sendBroadcastToActivity(BroadcastMessage broadcastMessage) {
        Log.l("EversongServiceLog:: sending broadcast " + broadcastMessage.toString());
        try {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(broadcastMessage.toString());

            sendBroadcast(broadCastIntent);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class EversongServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.l("EversongServiceLog:: Broadcast received " + intent.getAction());
            try {
                if (intent.getAction() != null) {
                    if(intent.getAction().equals(BroadcastMessage.START_RECORDING_AUDIO.toString())) {
                        sendBroadcastToActivity(BroadcastMessage.STOP_RECORDING_AUDIO);
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void registerEversongServiceBroadcastReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BroadcastMessage.REFRESH_FRAME.toString());
            intentFilter.addAction(BroadcastMessage.START_RECORDING_AUDIO.toString());
            intentFilter.addAction(BroadcastMessage.STOP_RECORDING_AUDIO.toString());
            registerReceiver(eversongBroadCastReceiver, intentFilter);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
