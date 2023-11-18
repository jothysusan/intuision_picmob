package com.picmob.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class SignalRService extends Service {

    private HubConnection mHubConnection;
    //    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private static final String TAG = "SignalRService";

    public SignalRService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR();
        return result;
    }

    private void startSignalR() {



        String serverUrl = "https://picmobproto02.azurewebsites.net/messages";
        mHubConnection = HubConnectionBuilder.create(serverUrl).build();
        mHubConnection.start().blockingAwait();

        Log.e(TAG, "startSignalR: " + mHubConnection.getConnectionState());

    }


    public void sendMessage(String message) {
        Log.e(TAG, "sendMessage: " + message);


        mHubConnection.send("SendMessage", "bob", "Florida", "Initiator", message);
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        startSignalR();
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

}
