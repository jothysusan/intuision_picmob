package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.GPS_REQUEST;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.picmob.android.R;
import com.picmob.android.mvvm.webservices.UnauthorizedEvent;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.GpsCallBack;
import com.picmob.android.utils.GpsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class BaseActivity extends AppCompatActivity implements GpsCallBack {
    //    implements GpsCallBack
    private static final String TAG = "BaseActivity";
    // A reference to the service used to get location updates.
    private LocationService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    private GpsUtils mGpsUtil;
    private boolean isGPS;
    private boolean isPermissionGranted;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Starting foreground service");
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            setGpsStatus();
            mService.locationIsOn(isGPS);
            if (!isGPS) {
                Log.i(TAG, getString(R.string.error_gps_not_enabled));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void setGpsStatus() {
        isGPS = isLocationEnabled();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        mGpsUtil = new GpsUtils(this);
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        if (checkPermissions()) {
            isPermissionGranted = true;
        }
        if (isPermissionGranted) {
//            bindService(new Intent(this, LocationService.class), mServiceConnection,
//                    Context.BIND_AUTO_CREATE);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "on resume");
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    private void turnGpsOn() {
        mGpsUtil.turnGPSOn(this);
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = true;
//                bindService(new Intent(this, LocationService.class), mServiceConnection,
//                        Context.BIND_AUTO_CREATE);
//                turnGpsOn();
            } else {
                isPermissionGranted = false;
                Log.d(TAG, "Location Permission denied. Use defaults.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST && mService != null) {
                mService.locationIsOn(true);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == GPS_REQUEST && mService != null) {
                mService.locationIsOn(false);
            }
        }
    }

    @Override
    public void gpsStatus(boolean isGPSEnable) {
        isGPS = isGPSEnable;
    }

    @Subscribe
    public final void onUnauthorizedEvent(UnauthorizedEvent e) {
        handleUnauthorizedEvent();
    }

    protected void handleUnauthorizedEvent() {
        CustomSharedPreference.getInstance(this).clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra(AppConstants.LOGOUT, AppConstants.LOGOUT);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
