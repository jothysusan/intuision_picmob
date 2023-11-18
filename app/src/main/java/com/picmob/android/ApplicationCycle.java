package com.picmob.android;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.LogCapture;

import java.util.List;

public class ApplicationCycle extends Application implements LifecycleObserver {

    private static Context appContext;
    public static boolean wasInBackground;
    private static final String TAG = "ApplicationCycle";
    private FirebaseCrashlytics mCrashlytics;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        mCrashlytics = FirebaseCrashlytics.getInstance();
    }

    public static Context getAppContext() {
        return appContext;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground

        LogCapture.e(TAG, "onMoveToForeground: ");
        wasInBackground = true;
        isAppIsInBackground(appContext);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        LogCapture.e(TAG, "onMoveToBackground: ");
        AppConstants.APP_FOREGROUND = true;
        wasInBackground = false;

    }


    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        Log.e(TAG, "isAppIsInBackground: " + activeProcess);
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                Log.e(TAG, "isAppIsInBackground: false");
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
