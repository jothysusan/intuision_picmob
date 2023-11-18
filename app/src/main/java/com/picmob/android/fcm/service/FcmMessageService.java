package com.picmob.android.fcm.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.picmob.android.R;
import com.picmob.android.activity.DmActivity;
import com.picmob.android.activity.HomeActivity;
import com.picmob.android.fcm.workers.FcmWorker;
import com.picmob.android.mvvm.dm.DmPojo;
import com.picmob.android.mvvm.dm.DmViewModel;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.home.FeedViewModel;
import com.picmob.android.mvvm.requests_response.RequestResponseViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;

public class FcmMessageService extends FirebaseMessagingService {

    private static final String TAG = "FcmMessageService";
    PendingIntent pendingIntent;
    private FeedViewModel fViewModel;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {

            AppConstants.NEW_DATA = true;
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());

            Log.e(TAG, "onMessageReceived: Title" + remoteMessage.getData().get("title") +
                    "\n Body=>" + remoteMessage.getData().get("message"));

            if (remoteMessage.getData().containsKey("type")) {

                doMsgAction(remoteMessage);

               /* if (remoteMessage.getData().get("type").equals("2")) {
                    FriendsPojo pojo = new FriendsPojo();
                    pojo.setId(Integer.parseInt(remoteMessage.getData().get("id")));
                    pojo.setUsername(remoteMessage.getData().get("username"));
                    pojo.setFirstName(remoteMessage.getData().get("firstName"));
                    pojo.setLastName(remoteMessage.getData().get("lastName"));
                    pojo.setEmail(remoteMessage.getData().get("email"));
                    pojo.setLocation(remoteMessage.getData().get("location"));
                    pojo.setPhoneNumber(remoteMessage.getData().get("phoneNumber"));
                    pojo.setNotifyToken(remoteMessage.getData().get("notifyToken"));
                    pojo.setFbToken(remoteMessage.getData().get("fbToken"));
                    pojo.setTwitterToken(remoteMessage.getData().get("twitterToken"));
                    pojo.setGoogleToken(remoteMessage.getData().get("googleToken"));
                    pojo.setFbId(remoteMessage.getData().get("fbId"));
                    pojo.setGoogleId(remoteMessage.getData().get("googleId"));
                    pojo.setTwitterId(remoteMessage.getData().get("twitterId"));
                    pojo.setAvatarURL(remoteMessage.getData().get("avatarURL"));

                    int type = Integer.parseInt(remoteMessage.getData().get("type"));

                    sendNotification(remoteMessage.getData().get("title"),
                            remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
                            pojo);
                } else {
                    int type = Integer.parseInt(remoteMessage.getData().get("type"));
                    sendNotification(remoteMessage.getData().get("title"),
                            remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
                            null);
                }*/

            } else {
                sendNotification(remoteMessage.getData().get("title"),
                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), 0, null, null);
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                scheduleJob();
            } else {

                handleNow();
            }

        }
       /* if (remoteMessage.getNotification() != null) {
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(), "", 0, null,null);
        }*/

    }

    private void doMsgAction(RemoteMessage remoteMessage) {
        int type = Integer.parseInt(remoteMessage.getData().get("type"));
        switch (type) {

            case 0:
                AppConstants.REQUEST_RESPONSE_UPDATE = true;
                sendNotification(remoteMessage.getData().get("title"),
                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
                        null, null);
                break;
            case 1:
                AppConstants.REQUEST_RESPONSE_UPDATE = true;
                sendNotification(remoteMessage.getData().get("title"),
                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
                        null, null);
                break;
            case 2:
                AppConstants.DM_UPDATE = true;
                FriendsPojo pojo = new FriendsPojo();
                pojo.setId(Integer.parseInt(remoteMessage.getData().get("user_id")));
                pojo.setUsername(remoteMessage.getData().get("username"));
                pojo.setFirstName(remoteMessage.getData().get("firstName"));
                pojo.setLastName(remoteMessage.getData().get("lastName"));
                pojo.setEmail(remoteMessage.getData().get("email"));
                pojo.setLocation(remoteMessage.getData().get("location"));
                pojo.setPhoneNumber(remoteMessage.getData().get("phoneNumber"));
                pojo.setNotifyToken(remoteMessage.getData().get("notifyToken"));
                pojo.setFbToken(remoteMessage.getData().get("fbToken"));
                pojo.setTwitterToken(remoteMessage.getData().get("twitterToken"));
                pojo.setGoogleToken(remoteMessage.getData().get("googleToken"));
                pojo.setFbId(remoteMessage.getData().get("fbId"));
                pojo.setGoogleId(remoteMessage.getData().get("googleId"));
                pojo.setTwitterId(remoteMessage.getData().get("twitterId"));
                pojo.setAvatarURL(remoteMessage.getData().get("avatarURL"));

                sendNotification(remoteMessage.getData().get("title"),
                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
                        pojo, null);
                break;
            case 3:
                GalleryPojo galleryPojo = new GalleryPojo();
                galleryPojo.setUserId(Integer.parseInt(remoteMessage.getData().get("user_id")));
                galleryPojo.setMakePublic(0);
                galleryPojo.setUserName(remoteMessage.getData().get("username"));
                galleryPojo.setDateTime(remoteMessage.getData().get("dateTime").toString());
                galleryPojo.setMessageId(Integer.parseInt(remoteMessage.getData().get("msg_id")));
                galleryPojo.setMediaURL(remoteMessage.getData().get("mediaUrl"));
                AppConstants.FEED_UPDATE = true;
//                sendNotification(remoteMessage.getData().get("title"),
//                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
//                        null, galleryPojo);

                sendNotification(
                        remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody(), "", type,
                        null, galleryPojo);
                break;
            case 4:
                fViewModel = new FeedViewModel();
                GalleryPojo galleryPojos = new GalleryPojo();
                galleryPojos.setUserId(Integer.parseInt(remoteMessage.getData().get("user_id")));
                galleryPojos.setMakePublic(1);
                galleryPojos.setUserName(remoteMessage.getData().get("username"));
                galleryPojos.setDateTime(remoteMessage.getData().get("dateTime").toString());
                galleryPojos.setMessageId(Integer.parseInt(remoteMessage.getData().get("msg_id")));
                galleryPojos.setMediaURL(remoteMessage.getData().get("mediaUrl"));
                AppConstants.FEED_UPDATE = true;
//                fViewModel.addFeed(galleryPojos);
                sendNotification(
                        remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody(), "", type,
                        null, galleryPojos);
//                sendNotification(remoteMessage.getData().get("title"),
//                        remoteMessage.getData().get("message"), remoteMessage.getData().get("body"), type,
//                        null, galleryPojos);
                break;

        }
    }

    private void scheduleJob() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(FcmWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
    }


    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.e(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        Log.e(TAG, "sendRegistrationToServer: " + token);
    }


    private void sendNotification(String title, String message, String msgBody, int type, FriendsPojo fPojo, GalleryPojo gPojo) {
       /* if (type == 1) {
            intent = new Intent(this, DmActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(AppConstants.MESSAGE_DETAILS, new Gson().toJson(pojo));
        } else {
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(AppConstants.MSG, AppConstants.MSG);
        }*/

        Log.e(TAG, "sendNotification: "+type );

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent = getIntentType(type,fPojo,gPojo);

        Log.e(TAG, "sendNotification: "+intent.getStringExtra(AppConstants.MESSAGE_DETAILS) );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            pendingIntent = PendingIntent.getActivity(this, 0,
                    intent,PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    /* PendingIntent.FLAG_ONE_SHOT*/);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    /* PendingIntent.FLAG_ONE_SHOT*/);
        }


        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_p)
                        .setColor(Color.parseColor("#FF6200EE"))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message + "\n" + msgBody))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
// broadcast
        notificationManager.notify(0 , notificationBuilder.build());
        Intent gcm_rec = new Intent("get_request");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gcm_rec);
    }

    private Intent getIntentType( int type, FriendsPojo fPojo, GalleryPojo gPojo) {
        Intent intent = new Intent();
        if (type ==0 ||type ==1){
            intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(AppConstants.TAB, 2);
        }
        else if (type ==2){
            intent = new Intent(this, DmActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(AppConstants.MESSAGE_DETAILS, new Gson().toJson(fPojo));
        }
        else if (type == 3 ||type == 4){
            intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(AppConstants.TAB, 0);
        }
        return intent;
    }


}
