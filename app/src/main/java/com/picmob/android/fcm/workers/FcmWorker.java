package com.picmob.android.fcm.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.picmob.android.utils.LogCapture;

public class FcmWorker extends Worker {

    private static final String TAG = "FcmWorker";

    public FcmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LogCapture.d("doWork: ");
        // TODO(developer): add long running task here.
        return Result.success();
    }
}
