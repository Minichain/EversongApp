package com.upf.minichain.eversongapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.upf.minichain.eversongapp.enums.BroadcastMessage;

public class EversongService extends Service {
    @Override
    public void onCreate() {
        Log.l("EversongServiceLog:: onCreate service");
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

    private void sendBroadCast(BroadcastMessage broadcastMessage) {
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
}
