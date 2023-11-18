package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.CAMERA;
import static com.picmob.android.utils.AppConstants.CAMERA_REQUEST;
import static com.picmob.android.utils.AppConstants.GALLERY_REQUEST;
import static com.picmob.android.utils.AppConstants.GALLERY_VIDEO_REQUEST;
import static com.picmob.android.utils.AppConstants.READ_STORE;
import static com.picmob.android.utils.AppConstants.RECORD;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.VIDEO_REQUEST;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;
import static com.picmob.android.utils.AppConstants.mPhotoPath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.implementation.BlobStorageService;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.dm.DmViewModel;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.RequestResponseViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.FileUtils;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.picmob.videocom.VideoCompress;
import com.videotrimmer.library.utils.CompressOption;
import com.videotrimmer.library.utils.TrimType;
import com.videotrimmer.library.utils.TrimVideo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;

public class RequestResponseActivity extends BaseActivity implements com.picmob.android.listeners.BlobStorageService {

    @BindView(R.id.imgThumb)
    ImageView imgThumb;
    @BindView(R.id.etMsg)
    EditText etMsg;
    @BindView(R.id.btnContinue)
    Button btnSnd;
    @BindView(R.id.btnRetake)
    Button btnRetake;
    @BindView(R.id.layMedia)
    LinearLayout layMedia;
    @BindView(R.id.video_view)
    PlayerView videoView;

    private String msgDetails;
    private RequestResponseModel model;
    private int transaction, action;
    private static final String TAG = "RequestResponseActivity";
    private UtilsFunctions utilsFunctions;
    private Uri outUri;
    private File compressedFile;
    private String blobUrl;
    private int eventId;
    private RequestResponseViewModel viewModel;
    private DmViewModel dmViewModel;
    private EventViewModel eventViewModel;
    private UserAuthPojo userAuthPojo;
    private FriendsPojo friendsPojo;

    private ProgressDialog progressDialog;
    SimpleExoPlayer mPlayer;
    DefaultDataSource.Factory dataSourceFactory;
    ImageView fullscreenButton;
    ImageButton exoPlayButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        ButterKnife.bind(this);

        mPlayer = new SimpleExoPlayer.Builder(this).build();
        fullscreenButton = videoView.findViewById(R.id.exo_fullscreen_icon);
        exoPlayButton = videoView.findViewById(R.id.exo_play);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UtilsFunctions.isNetworkAvail(RequestResponseActivity.this)) {
                    Intent i = new Intent(RequestResponseActivity.this,
                            VideoPlayerActivity.class);
                    i.putExtra(AppConstants.VIDEO_URL, outUri != null ?
                            outUri.toString() : model.getMediaURL());
                    startActivity(i);
                }
            }
        });
        videoView.setPlayer(mPlayer);
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    exoPlayButton.setImageResource(R.drawable.ic_replay);
                } else {
                    exoPlayButton.setImageResource(R.drawable.exo_icon_play);
                }
            }
        });
        dataSourceFactory = new DefaultDataSourceFactory(this);

        utilsFunctions = new UtilsFunctions();
        SpannableString spannableString = new SpannableString("Message");

        if (getIntent().getExtras() != null) {
            transaction = getIntent().getIntExtra(AppConstants.TRANSACTION, 0);
            action = getIntent().getIntExtra(AppConstants.ACTION, 0);

            if (transaction == AppConstants.RESPOND) {
                spannableString = new SpannableString("Respond");
                btnRetake.setVisibility(View.GONE);
            } else if (transaction == AppConstants.REQUEST) {
                spannableString = new SpannableString("Request");
                btnRetake.setVisibility(View.GONE);
            } else if (transaction == AppConstants.NEW_MESSAGE) {
                spannableString = new SpannableString("Message");
                btnRetake.setVisibility(View.GONE);
            } else if (transaction == AppConstants.EVENT) {
                spannableString = new SpannableString(getString(R.string.event));
                eventId = getIntent().getIntExtra(AppConstant.EVENT_ID, 0);
                btnRetake.setVisibility(View.GONE);
            }

            if (getIntent().getExtras().containsKey(AppConstants.MESSAGE_DETAILS)) {
                msgDetails = getIntent().getStringExtra(AppConstants.MESSAGE_DETAILS);
                model = new Gson().fromJson(msgDetails, RequestResponseModel.class);
            }

            if (getIntent().getExtras().containsKey(AppConstants.FRIEND_DETAILS)) {
                friendsPojo = new Gson().fromJson(getIntent().getStringExtra(AppConstants.FRIEND_DETAILS), FriendsPojo.class);
            }

        }

        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        viewModel = ViewModelProviders.of(this).get(RequestResponseViewModel.class);
        dmViewModel = ViewModelProviders.of(this).get(DmViewModel.class);
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);

        /*imgPlay.setVisibility(View.GONE);*/
//        checkPersmission();
    }


    @OnClick(R.id.btnRetake)
    void reTake() {
        if (UtilsFunctions.isNetworkAvail(RequestResponseActivity.this))
            doAction();
    }


    @OnClick(R.id.btnContinue)
    void send() {

        if (UtilsFunctions.isNetworkAvail(RequestResponseActivity.this)) {

            if (transaction == AppConstants.NEW_MESSAGE || transaction == AppConstants.REQUEST) {
                if (!etMsg.getText().toString().trim().isEmpty()) {
                    respondMessage();
                } else
                    UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.enter_message));
            } else
                respondMessage();
        }
    }

    @OnClick(R.id.layMedia)
    void action() {
        if (UtilsFunctions.isNetworkAvail(RequestResponseActivity.this))
            checkPersmission();
    }


    private void respondMessage() {
        if (transaction == AppConstants.NEW_MESSAGE) {
            if (compressedFile != null) {
                utilsFunctions.showDialog(RequestResponseActivity.this);
                new BlobStorageService(this).uploadFile(compressedFile);
            } else {
                sendAndReceive(null);
            }
        } else if (transaction == AppConstants.REQUEST) {
            if (compressedFile != null) {
                if (!etMsg.getText().toString().trim().isEmpty()) {
                    utilsFunctions.showDialog(RequestResponseActivity.this);
                    new BlobStorageService(this).uploadFile(compressedFile);
                } else
                    UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.enter_message));
            } else {
                sendAndReceive(null);
            }
        } else if (transaction == AppConstants.EVENT) {
            if (compressedFile != null) {
                utilsFunctions.showDialog(RequestResponseActivity.this);
                new BlobStorageService(this).uploadFile(compressedFile);
            } else {
                UtilsFunctions.showToast(this, AppConstants.longToast, getString(R.string.please_add_photo_or_video));
            }
        } else {
            if (compressedFile != null) {
                utilsFunctions.showDialog(RequestResponseActivity.this);
                new BlobStorageService(this).uploadFile(compressedFile);
            } else
                UtilsFunctions.showToast(this, AppConstants.longToast, getString(R.string.please_add_photo_or_video));
        }
    }


    public void showDialog() {
       /* progressDialog = new ProgressDialog(context);
        progressDialog.create();
        progressDialog.setMessage("Please wait...");*/
        progressDialog = new ProgressDialog(this);
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

    private void checkPersmission() {
        if (UtilsFunctions.hasMultiplePermissions(this, READ_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, CAMERA)
                && UtilsFunctions.hasMultiplePermissions(this, RECORD)) {
            Log.e(TAG, "checkPersmission: doAction");
            doAction();
        } else {
            Log.e(TAG, "checkPersmission: doAction");
            ActivityCompat.requestPermissions(this, new String[]{READ_STORE, WRITE_STORE, CAMERA, RECORD}, STORE_PERMISSION);
        }
    }


    private void doAction() {
      /*  if (action == AppConstants.VIDEO)
            videoCapture();
        else
            cameraCapture();*/
        if (UtilsFunctions.isNetworkAvail(RequestResponseActivity.this))
            showRespondSheet();
    }


    private void videoCapture() {
       /* Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = UtilsFunctions.createVideoFile(this);
            } catch (IOException ex) {
                LogCapture.e(TAG, "videoCapture:" + ex.getLocalizedMessage());
            }
            if (photoFile != null) {

                takePictureIntent.putExtra("output", FileProvider.getUriForFile(this,
                        AppConstants.FILE_PROVIDER, photoFile));
                startActivityForResult(takePictureIntent, VIDEO_REQUEST);
            }
        }*/

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {

            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            try {
                compressedFile = UtilsFunctions.createVideoFile(this);
            } catch (IOException ex) {
                LogCapture.e(TAG, "videoCapture:" + ex.getLocalizedMessage());
            }

            outUri = FileProvider.getUriForFile(this, AppConstants.FILE_PROVIDER, compressedFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
            takeVideoIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            takeVideoIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            LogCapture.e(TAG, "videoCapture: " + outUri.toString());
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, VIDEO_REQUEST);
            }

        }


      /*  Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, VIDEO_REQUEST);
        }*/

    }

//    private void cameraCapture(){
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
//        contentSelectionIntent.setType("*/*");
//        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent,takeVideoIntent});
//        startActivityForResult(chooserIntent, 1);
//    }

    @SuppressLint("QueryPermissionsNeeded")
    private void cameraCapture() {
        Log.e(TAG, "cameraCapture: ");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                compressedFile = UtilsFunctions.createImageFile(this);
            } catch (IOException ex) {
                LogCapture.e(TAG, "takeCameraPicture:" + ex.getLocalizedMessage());
            }
            if (compressedFile != null) {

                outUri = FileProvider.getUriForFile(this, AppConstants.FILE_PROVIDER, compressedFile);
                Log.e(TAG, "cameraCapture: " + outUri.toString());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);

//                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            checkPersmission();
            LogCapture.e(TAG, "Permission Granted, Now you can use local drive .CONFIRMED");
        } else {
            LogCapture.e(TAG, "Permission Denied, You cannot use local drive .");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST || requestCode == GALLERY_REQUEST) {
                try {
                    /*imgPlay.setVisibility(View.GONE);*/
                    btnRetake.setVisibility(View.VISIBLE);
                    layMedia.setVisibility(View.GONE);
                    imgThumb.setVisibility(View.VISIBLE);
                    imgThumb.setOnClickListener(v -> {
                        Intent intent = new Intent(this, ImagePreviewActivity.class);
                        intent.putExtra(AppConstants.MEDIA, outUri.toString());
                        startActivity(intent);
                    });
                    videoView.setVisibility(View.GONE);
                    if (requestCode == GALLERY_REQUEST) {
                        // check for file path
                        outUri = data != null ? data.getData() : null;
                    }
                    Glide.with(RequestResponseActivity.this)
                            .load(outUri)
                            .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                            .error(R.drawable.ic_landscape).into(imgThumb);
                    if (requestCode == CAMERA_REQUEST) {
                        compressedFile = new Compressor(this)
                                .compressToFile(new File(AppConstants.mPhotoPath));
                    } else {
                        compressedFile = new Compressor(this)
                                .compressToFile(FileUtils.getFileFromUri(this, outUri));
//                        new File(getUriRealPath(this, outUri))
//                        FileUtils.getFileFromUri(this, eventImageURI)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == VIDEO_REQUEST) {
                try {
                    /*imgPlay.setVisibility(View.VISIBLE);*/
                    btnRetake.setVisibility(View.VISIBLE);
                    layMedia.setVisibility(View.GONE);
                    imgThumb.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
//                    outUri = data.getData();
                    ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(outUri));
                    mPlayer.setMediaSource(mediaSource);
                    mPlayer.prepare();
//                    Glide.with(RequestResponseActivity.this)
//                            .asBitmap()
//                            .load(UtilsFunctions.getThumbnail(mPhotoPath)).placeholder(R.drawable.ic_landscape).into(imgThumb);
//                    compressedFile = FileUtils.getFileFromUri(RequestResponseActivity.this, outUri);
                    compressedFile = UtilsFunctions.getVideoFile();
                    compressAndUpload(compressedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == GALLERY_VIDEO_REQUEST) {
                TrimVideo.activity(String.valueOf(data != null ? data.getData() : null))
                        .setHideSeekBar(false)
                        .setCompressOption(new CompressOption())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(1, 10)
                        .start(this);
            }
            if (requestCode == TrimVideo.VIDEO_TRIMMER_REQ_CODE && data != null) {
                btnRetake.setVisibility(View.VISIBLE);
                layMedia.setVisibility(View.GONE);
                imgThumb.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                outUri = Uri.parse(TrimVideo.getTrimmedVideoPath(data));
                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(outUri));
                mPlayer.setMediaSource(mediaSource);
                mPlayer.prepare();
                Log.d(TAG, "Trimmed path:: " + outUri);
                compressedFile = new File(TrimVideo.getTrimmedVideoPath(data));
                deleteOtherFilesFromFolder(compressedFile);
            }
        }
    }

    private void deleteOtherFilesFromFolder(File compressedFile) {
        File dir = new File(compressedFile.getParent());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!child.equals(compressedFile.getName()))
                    new File(dir, child).delete();
            }
        }
    }

    private String getUriRealPath(Context ctx, Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null,
                null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null
                , MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;

    }

    public void showRespondSheet() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_respond, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        LinearLayout vCam = dialogView.findViewById(R.id.vCam);
        LinearLayout vVideo = dialogView.findViewById(R.id.vVideo);

        vCam.setOnClickListener(v -> {
            showPickerOption(AppConstants.PICTURE);
            dialog.dismiss();
        });

        vVideo.setOnClickListener(v -> {
            showPickerOption(AppConstants.VIDEO);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showPickerOption(int mediaType) {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_file, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        LinearLayout vCam = dialogView.findViewById(R.id.vCam);
        LinearLayout vImg = dialogView.findViewById(R.id.vImg);
        LinearLayout vVideo = dialogView.findViewById(R.id.vVideo);
        LinearLayout vDoc = dialogView.findViewById(R.id.vDoc);
        vVideo.setVisibility(View.GONE);
        vDoc.setVisibility(View.GONE);
        vCam.setOnClickListener(v -> {
            fileAction(mediaType == AppConstants.PICTURE ? CAMERA_REQUEST : VIDEO_REQUEST);
            dialog.dismiss();
        });

        vImg.setOnClickListener(v -> {
            fileAction(mediaType == AppConstants.PICTURE ? GALLERY_REQUEST : GALLERY_VIDEO_REQUEST);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void fileAction(int action) {
        switch (action) {
            case CAMERA_REQUEST:
                cameraCapture();
                break;
            case VIDEO_REQUEST:
                videoCapture();
                break;
            case GALLERY_REQUEST:
                getGalleryImage();
                break;
            case GALLERY_VIDEO_REQUEST:
                getGalleryVideo();
                break;
        }
    }

    private void getGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST);

    }

    private void getGalleryVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_VIDEO_REQUEST);
    }

    private void compressAndUpload(File videoFile) {
        VideoCompress.compressVideoMedium(mPhotoPath, videoFile.getPath(), new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart: compression started=>" + mPhotoPath);
            }

            @Override
            public void onSuccess() {
                Log.e(TAG, "onSuccess: compression ended=>" + videoFile.getAbsolutePath());
                compressedFile = videoFile;
            }

            @Override
            public void onFail() {
                UtilsFunctions.showToast(RequestResponseActivity.this, AppConstants.shortToast, "Unable to upload this file!");
            }

            @Override
            public void onProgress(float percent) {
            }
        });
    }


    Observer<Resource<ApiResponseModel>> respondObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                  /*  RequestResponseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideDialog();
                        }
                    });*/
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(RequestResponseActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                   /* RequestResponseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideDialog();
                        }
                    });*/
                    AppConstants.REQUEST_RESPONSE_UPDATE = true;
                    utilsFunctions.hideDialog();
                    Intent i = new Intent(RequestResponseActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    UtilsFunctions.showToast(RequestResponseActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };


    Observer<Resource<ApiResponseModel>> sendDmObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(RequestResponseActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    AppConstants.TRANS = 1;
                    finish();
                    UtilsFunctions.showToast(RequestResponseActivity.this, AppConstants.shortToast,
                            getString(R.string.message_sent));
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> attachImageObs = new Observer<Resource<ApiResponseModel>>() {

        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(RequestResponseActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    String v = utilsFunctions.getFileType(compressedFile.toString());
                    utilsFunctions.hideDialog();
                    setResult(Activity.RESULT_OK);
                    finish();
                    UtilsFunctions.showToast(RequestResponseActivity.this, AppConstants.shortToast,
                            !v.equals("video") ? getString(R.string.image_attached) : getString(R.string.video_attached));
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };

    @Override
    public void getUrl(URI url) {
        Log.e(TAG, "getUrl: " + url);
        blobUrl = url.toString();
        sendAndReceive(blobUrl);
    }

    private void sendAndReceive(String blobUrl) {
        if (transaction == AppConstants.RESPOND) {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.RES_UID, userAuthPojo.getId());
            map.put(AppConstant.M_ID, model.getMessageId());
            map.put(AppConstant.MSG, etMsg.getText().toString().trim());
            map.put(AppConstant.DATETIME, new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").format(new Date()));
            if (blobUrl != null)
                map.put(AppConstant.MEDIA_URL, blobUrl);
            viewModel.sendReponse(userAuthPojo.getToken(), map);
            RequestResponseActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewModel.sendRespondData().observe(RequestResponseActivity.this,
                            RequestResponseActivity.this.respondObserver);
                }
            });
        } else if (transaction == AppConstants.REQUEST) {
            Intent i = new Intent(RequestResponseActivity.this, UserMapActivity.class);
            i.putExtra(AppConstants.MSG, etMsg.getText().toString().trim());
            if (blobUrl != null)
                i.putExtra(AppConstants.MEDIA, blobUrl);
            startActivity(i);
        } else if (transaction == AppConstants.NEW_MESSAGE) {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.SENDER_ID, userAuthPojo.getId());
            map.put(AppConstant.RECEIVER_ID, friendsPojo.getId());
            map.put(AppConstant.MSG, etMsg.getText().toString().trim());
            map.put(AppConstant.DATETIME, new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").format(new Date()));
            if (blobUrl != null)
                map.put(AppConstant.MEDIA_URL, blobUrl);
            dmViewModel.sendDM(userAuthPojo.getToken(), map);

            RequestResponseActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dmViewModel.getDmMsgData().observe(RequestResponseActivity.this, sendDmObs);
                }
            });
        } else if (transaction == AppConstants.EVENT) {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.USR_ID, userAuthPojo.getId());
            map.put(AppConstant.EVENT_ID, eventId);
            map.put(AppConstant.MESSAGE, etMsg.getText().toString().trim());
            map.put(AppConstant.DATETIME, new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").format(new Date()));
            if (blobUrl != null)
                map.put(AppConstant.MEDIA_URL, blobUrl);
            eventViewModel.attachEventImage(userAuthPojo.getToken(), map);
            RequestResponseActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    eventViewModel.attachEventImageData().observe(RequestResponseActivity.this, attachImageObs);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void error(String error) {
        utilsFunctions.hideDialog();
        Log.e(TAG, "error: " + error);
    }


    @Override
    protected void onDestroy() {
        /*hideDialog();*/
        utilsFunctions.hideDialog();
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        utilsFunctions.hideDialog();
        mPlayer.pause();
        super.onPause();
    }
}
