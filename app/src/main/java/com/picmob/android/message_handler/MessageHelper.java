package com.picmob.android.message_handler;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.picmob.android.utils.AppConstants;
import com.microsoft.signalr.HubConnection;

public class MessageHelper {

    private HubConnection hubConnection;
    private static final String TAG = "MessageHelper";
    private MsgHandler msgHandler;


    public MessageHelper(MsgHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    public void startMsgHub() {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run() {

            }
        });

//        new HubConnectionTask().execute(hubConnection);

    }

    public void setupMsgSession(String user, String group, String role) {


        AsyncTask.execute(() -> hubConnection.invoke(AppConstants.setupSession, user, group, role).toFlowable());
        hubConnection.on("UserConnected", (msg) -> {
            Log.e(TAG, "UserConnected: " + msg);
        }, String.class);
    }


    public void checkMessage() {
        hubConnection.on("ReceiveMessage", (msg) -> {
            msgHandler.onReceiveMsg(msg);
            Log.e(TAG, "ReceiveMessage: " + msg);
        }, String.class);
    }


    public void sendMsg(int role, String group, String msg) {

        Log.e(TAG, "sendMsg: " + role + " group=>" + group + "  msg=>" + msg);

        switch (role) {
            case 1:
                hubConnection.send(AppConstants.sendGrpMsg, group, msg);
                break;
            case 2:
                hubConnection.send(AppConstants.sendMsgToInitiator, group, msg);
                break;
        }
    }


    public void sndMsg(String usr, String role, String grp, String msg) {


        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run() {
                hubConnection.send("SendMessage", usr, grp, role, msg);
            }
        });


    }

   /* class HubConnectionTask extends AsyncTask<HubConnection, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(HubConnection... hubConnections) {
            HubConnection hubConnection = hubConnections[0];
            hubConnection.start().blockingAwait();
            return null;
        }
    }*/
}
