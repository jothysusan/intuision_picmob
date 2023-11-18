package com.picmob.android.activity;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.LogCapture;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {
    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    private static final String TAG = LocationService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    private boolean isGPS = true;
    private boolean isRequestingLocationUpdates;
    Timer trackingTimer;
    private int TIME_INTERVAL_30_SEC = 30000;
    public int counter = 0;
    private UserAuthPojo userAuthPojo;
    PendingIntent servicePendingIntent;

    @Override
    public void onCreate() {
        startTimer();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
//        Service handling code
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
//      Notification Manager To create notification channel
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }

    private void onNewLocation(Location location) {
        removeLocationUpdates();
        resetTimer();
        mLocation = location;
        startApiCall();
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    public void startTimer() {
        stopTimerTask();
        //set a new Timer
        trackingTimer = new Timer();
        //schedule the timer, to wake up
        trackingTimer.schedule(initializeTimerTask(), TIME_INTERVAL_30_SEC, TIME_INTERVAL_30_SEC);
    }

    public void resetTimer() {
        stopTimerTask();
        trackingTimer = new Timer();
        trackingTimer.schedule(initializeTimerTask(), TIME_INTERVAL_30_SEC, TIME_INTERVAL_30_SEC);

    }

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void setGpsStatus() {
        isGPS = isLocationEnabled();
    }

    public TimerTask initializeTimerTask() {
        return new TimerTask() {
            public void run() {
                setGpsStatus();
                if (isGPS && !isRequestingLocationUpdates) {
                    startLocationUpdate();
                    if (counter < 5) {
                        counter++;
                    } else {
                        counter = 5;
                    }
                    Log.i("in timer", "in count: " + (counter));
                } else {
                    if (serviceIsRunningInForeground(getApplicationContext())) {
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                        }
                    }

                }
            }
        };
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (trackingTimer != null) {
            trackingTimer.cancel();
            trackingTimer = null;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    private void startLocationUpdate() {
        Log.i(TAG, "Requesting location updates");
        isRequestingLocationUpdates = true;
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException unlikely) {
            isRequestingLocationUpdates = false;
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }


    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void startApiCall() {
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        if (userAuthPojo != null) {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.ID, userAuthPojo.getId());
            if (mLocation != null) {
                // TODO: note to remember that the key need to be updated
                map.put("lattitude", String.valueOf(mLocation.getLatitude()));
                map.put(AppConstant.LONGITUDE, String.valueOf(mLocation.getLongitude()));
            } else {
                map.put("lattitude", "");
                map.put(AppConstant.LONGITUDE, "");
            }

            LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Authorization", "Bearer " + userAuthPojo.getToken());

            ApiInterface service = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseBody> call = service.updateCurrentLocation(headerMap, map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//            on Success updating the server with current location of the  user
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    LogCapture.e(TAG, t.getMessage());
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        // When app goes background

        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service");
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        stopTimerTask();
    }


    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationService.class);
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
//        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
//                PendingIntent.FLAG_IMMUTABLE);
//        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                //TODO: Need to remove action
                .addAction(R.drawable.ic_p, getString(R.string.stop_service),
                        servicePendingIntent)
                .setContentTitle(isGPS ? getString(R.string.gps_enabled_text) : getString(R.string.gps_disabled_text))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_p)
                .setSound(null)
                .setWhen(System.currentTimeMillis());
        return builder.build();
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            isRequestingLocationUpdates = false;
        } catch (SecurityException unlikely) {
            isRequestingLocationUpdates = true;
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void locationIsOn(boolean mIsGPS) {
        isGPS = mIsGPS;
        if (mIsGPS) {
            startTimer();
            startService(new Intent(getApplicationContext(), LocationService.class));
        } else {
            stopTimerTask();
        }
    }

}
