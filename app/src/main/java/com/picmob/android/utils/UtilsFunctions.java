package com.picmob.android.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.picmob.android.R;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsFunctions {

    private static AlertDialog alertDialog;
    public ProgressDialog progressDialog;
    private static final String TAG = "UtilsFunctions";

    private Uri outputFileUri;

    public static void showToast(Context context, int tost, String msg) {

        switch (tost) {
            case AppConstants.shortToast:
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                break;
            case AppConstants.longToast:
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                break;
        }
    }


    public static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = new View(activity);
        }
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }


    public void showDialog(Context context) {
       /* progressDialog = new ProgressDialog(context);
        progressDialog.create();
        progressDialog.setMessage("Please wait...");*/
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public static boolean hasMultiplePermissions(@NonNull Context context, @NonNull String str) {
        boolean permiss = false;
        int result = ContextCompat.checkSelfPermission(context, str);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return permiss;
    }

    public static File createImageFile(Context context) throws IOException {

        File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob/images");
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob");

        if (!directory.exists()) {
            directory.mkdirs();

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(directory.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!files.exists()) {
            files.mkdirs();
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(files.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_IMG_" + timeStamp;

        File destFile = new File(files.getAbsoluteFile() + "/" + sb + ".jpg");
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

     /*   String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_" + timeStamp;
        File image = File.createTempFile(sb, ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_DCIM));
        image.createNewFile();*/
        AppConstants.mPhotoPath = destFile.getAbsolutePath();
        return destFile;
    }

    public static File createVideoFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_" + timeStamp;
        File image = File.createTempFile(sb, ".mp4", context.getExternalFilesDir(Environment.DIRECTORY_DCIM));
        image.createNewFile();
        AppConstants.mPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static Uri getCaptureImageOutputUri(Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_" + timeStamp;
        Uri outputFileUri = null;
        File getImage = context.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), sb + ".jpg"));
        }
        return outputFileUri;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static boolean checkPswrd(String pswrd) {
        boolean valid = false;
        if (stringLength(pswrd))
            if (pswrd.length() >= 6)
                valid = true;
        return valid;
    }

    public static boolean checkCpswrd(String pswrd, String cpswrd) {
        boolean valid = false;
        if (stringLength(pswrd))
            if (pswrd.equals(cpswrd))
                valid = true;
        return valid;
    }

    public static boolean checkPhone(String phone) {
        boolean valid = false;
        if (stringLength(phone))
            valid = validOnlyNumbers(phone);

        return valid;
    }


    public static boolean checkUserName(String usr) {
        boolean valid = false;
        valid = stringLength(usr);
        return valid;
    }


    public static boolean checkEmail(String email) {
        boolean valid = false;
        if (stringLength(email))
            valid = isEmailValid(email);
        return valid;
    }

    public static boolean checkName(String name) {
        boolean valid = false;
        if (stringLength(name)) {
            if (!checkSpecialChar(name)) {
                valid = true;
                if (!checkNumber(name))
                    valid = true;
            }
        }
        return valid;
    }


    public static boolean isEmailValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    public static boolean stringLength(String str) {
        boolean valid = false;
        if (str != null && str.length() > 1)
            valid = true;
        return valid;
    }

    public static boolean validateUrl(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        boolean urlValid = false;

        try {
            new URL(url).toURI();
            urlValid = true;
        } catch (Exception e) {
            urlValid = false;
        }

        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(url);

        if (matcher.matches())
            urlValid = true;
        else
            urlValid = false;

        return urlValid;
    }

    public static boolean checkSpecialChar(String str) {
        Pattern my_pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher my_match = my_pattern.matcher(str);
        return my_match.find();
    }


    public static boolean checkNumber(String str) {
        return str.matches(".*\\d.*");
    }

    public static boolean validOnlyNumbers(String str) {
        String regex = "[0-9]+";

        Pattern p = Pattern.compile(regex);
        if (str == null)
            return false;

        Matcher m = p.matcher(str);
        return m.matches();
    }

  /*  public static String compressImage(Context context, Uri uri) {

        File files = new File(Environment.getExternalStorageDirectory() + File.separator + "Picmob/pictures");
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Picmob");

        if (!directory.exists())
            directory.mkdirs();

        if (!files.exists())
            files.mkdirs();

       *//* String filePath = SiliCompressor.with(context).compress(uri.toString(), files, true);
        LogCapture.e(TAG, "compressImage: " + filePath);*//*

        return filePath;
    }*/


    public static File getImageFile() throws URISyntaxException {

        File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob/images");
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob");

        if (!directory.exists()) {
            directory.mkdirs();

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(directory.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!files.exists()) {
            files.mkdirs();
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(files.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_VID_" + timeStamp;


        File destFile = new File(files.getAbsoluteFile() + "/" + sb + ".jpg");
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e(TAG, "compressVideo: " + destFile);


        return destFile;

    }

    public static File getVideoFile() throws URISyntaxException {

        File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob/videos");
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Picmob");

        if (!directory.exists()) {
            directory.mkdirs();

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(directory.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!files.exists()) {
            files.mkdirs();
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                try {
                    Files.createDirectory(Paths.get(files.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS").format(new Date());
        String sb = "PM_VID_" + timeStamp;


        File destFile = new File(files.getAbsoluteFile() + "/" + sb + ".mp4");
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e(TAG, "compressVideo: " + destFile);


        return destFile;

    }

    public static Bitmap getThumbnail(String path) {

        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
    }


  /*  public static Bitmap retriveVideoFrameFromVideo(Context context,String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }*/


    public static Bitmap retriveVideoFrameFromVideo(Context context, String videoPath) throws Throwable {

        final Bitmap[] bitmap = {null};

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                MediaMetadataRetriever mediaMetadataRetriever = null;
                try {
                    mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
                    //   mediaMetadataRetriever.setDataSource(videoPath);
                    bitmap[0] = mediaMetadataRetriever.getFrameAtTime();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } finally {
                    if (mediaMetadataRetriever != null) {
                        try {
                            mediaMetadataRetriever.release();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });


        return bitmap[0];
    }

    public static String getFileType(String url) {

        String fileType = null;

        Log.e(TAG, "getFileType: " + url);

        String extension = url.substring(url.lastIndexOf("."));

        if (extension.contains("mp4") || extension.contains("mov")
                || extension.contains("avi") || extension.contains("webm")
                || extension.contains("MPEG4") || extension.contains("MOV"))
            fileType = AppConstants.VIDEOS;

        if (extension.contains("jpg") || extension.contains("jpeg")
                || extension.contains("png"))
            fileType = AppConstants.PICTURES;

        return fileType;
    }


    public static class DownloadBitmapVideo extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... strings) {

            Bitmap bitmap = null;
            MediaMetadataRetriever mediaMetadataRetriever = null;
            try {
                mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(strings[0], new HashMap<String, String>());
                //   mediaMetadataRetriever.setDataSource(videoPath);
                bitmap = mediaMetadataRetriever.getFrameAtTime();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            } finally {
                if (mediaMetadataRetriever != null) {
                    try {
                        mediaMetadataRetriever.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);


        }
    }


    public static Drawable setRandomColor(Context context) {

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.image_shape);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);

        int color = ((int) (Math.random() * 16777215)) | (0xFF << 24);
        DrawableCompat.setTint(wrappedDrawable, color);

        return unwrappedDrawable;
    }

    public static String replaceX(int len) {

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append('X');
        }
        return sb.toString();
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    public static boolean isNetworkAvail(Context context) {
        boolean status = false;
        if (!isNetworkConnected(context)) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.no_internet_message))
                    .setPositiveButton(R.string.ok, null);
            alertDialog = builder.create();
            alertDialog.show();
        } else
            status = true;
        return status;
    }

    public static void showConfirmationDialog(Context context, String message,
                                              String positiveButton, String negativeButton,
                                              DialogInterface.OnClickListener dialogClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton(positiveButton, dialogClickListener)
                .setNegativeButton(negativeButton, dialogClickListener)
                .show();

    }

    public static void startDownload(String mediaURL, Context context) {
        if (isNetworkAvail(context)) {
            Uri downloadUri = null;
            DownloadManager downloadManager;
            String mimeType = null;
            try {
                downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                downloadUri = Uri.parse(mediaURL);
                String extension = downloadUri.getPath().substring(downloadUri.getPath().lastIndexOf("."));
                String prefX = "picmob-" + UUID.randomUUID().toString();
                String fileName = prefX + extension;
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                if (UtilsFunctions.getFileType(mediaURL).equalsIgnoreCase(AppConstants.PICTURES)) {
                    mimeType = "image/" + extension.substring(extension.lastIndexOf(".") + 1);

                }
                if (UtilsFunctions.getFileType(mediaURL).equalsIgnoreCase(AppConstants.VIDEOS)) {
                    mimeType = "video/" + extension.substring(extension.lastIndexOf(".") + 1);

                }
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(context.getResources().getString(R.string.picmob))
                        .setMimeType(mimeType)
                        // Display download progress and status message in notification bar
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                File.separator + fileName);
                downloadManager.enqueue(request);
                UtilsFunctions.showToast(context, AppConstants.shortToast, context.getString(R.string.start_download));
            } catch (Exception e) {
                UtilsFunctions.showToast(context, AppConstants.shortToast,
                        context.getString(R.string.download_failed) + " " + e.getMessage());
            }
        }
    }

    public static CircularProgressDrawable getCircularProgressDrawable(Context context) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();
        return circularProgressDrawable;
    }
}
