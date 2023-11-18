package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.events.EventImage;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.friends.FriendsViewModel;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.RequestResponseViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GalleryViewActivity extends BaseActivity {

    @BindView(R.id.imgThumb)
    ImageView imgThumb;
    @BindView(R.id.tvPrivate)
    TextView tvPrivate;
    @BindView(R.id.tvUser)
    TextView tvUser;
    @BindView(R.id.tvMsg)
    TextView tvMsg;
    @BindView(R.id.btnSnd)
    Button btnSnd;
    @BindView(R.id.btnIgnore)
    Button btnIgnore;
    @BindView(R.id.video_view)
    PlayerView videoView;
    @BindView(R.id.layAction)
    LinearLayout layAction;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tbPubilicity)
    ToggleButton tbPubilicity;
    @BindView(R.id.tbPublicityLayout)
    ConstraintLayout tbPublicityLayout;
    @BindView(R.id.tbPublicityView)
    View tbPublicityView;
    @BindView(R.id.layoutDownloadDelete)
    LinearLayout layoutDownloadDelete;
    @BindView(R.id.btnDownload)
    Button btnDownload;
    @BindView(R.id.btnDelete)
    Button btnDelete;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private UserAuthPojo userAuthPojo;
    EventViewModel eventViewModel;
    private String msgDetails;
    private RequestResponseModel model;
    private GalleryPojo gPojo;
    private EventImage eventImagePojo;
    private boolean mRequsted = false;
    private RequestResponseViewModel viewModel;
    private FriendsViewModel friendsViewModel;
    private UserAuthPojo userPojo;
    private static final String TAG = "GalleryViewActivity";
    private UtilsFunctions utilsFunctions;
    private boolean requestResponse = false;
    private int friendId;
    private SimpleExoPlayer mPlayer;
    DefaultDataSource.Factory dataSourceFactory;
    int fromScreen;
    String requested_users = "";
    String comma_separator = ", ";
    ImageView fullscreenButton;
    ImageButton exoPlayButton;
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_view);
        ButterKnife.bind(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        utilsFunctions = new UtilsFunctions();
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        viewModel = ViewModelProviders.of(this).get(RequestResponseViewModel.class);
        friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);

        userPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        mPlayer = new SimpleExoPlayer.Builder(this).build();
        fullscreenButton = videoView.findViewById(R.id.exo_fullscreen_icon);
        exoPlayButton = videoView.findViewById(R.id.exo_play);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this)) {
                    String url;
                    if (eventImagePojo != null) {
                        url = eventImagePojo.getImageUrl();
                    } else if (gPojo != null) {
                        url = gPojo.getMediaURL();
                    } else {
                        url = model.getMediaURL();
                    }
                    Intent i = new Intent(GalleryViewActivity.this, VideoPlayerActivity.class);
                    i.putExtra(AppConstants.VIDEO_URL, url);
                    startActivity(i);
                }
            }
        });

        videoView.setPlayer(mPlayer);
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                UtilsFunctions.isNetworkAvail(GalleryViewActivity.this);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    exoPlayButton.setImageResource(R.drawable.ic_replay);
                } else {
                    exoPlayButton.setImageResource(R.drawable.exo_icon_play);
                }
                if (playbackState == Player.STATE_BUFFERING)
                    progressBar.setVisibility(View.VISIBLE);
                else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED
                        || playbackState == Player.STATE_IDLE)
                    progressBar.setVisibility(View.INVISIBLE);
            }
        });
        dataSourceFactory = new DefaultDataSourceFactory(this);

        SpannableString spannableString = new SpannableString("Message");

        if (getIntent().getExtras() != null) {
            if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this)) {
                int screenFrom = getIntent().getIntExtra(AppConstants.SCREEN, 0);
                if (screenFrom == AppConstants.GALLERY_SCREEN) {
                    requestResponse = true;
                    msgDetails = getIntent().getStringExtra(AppConstants.MESSAGE_DETAILS);
                    gPojo = new Gson().fromJson(msgDetails, GalleryPojo.class);
                    fromScreen = AppConstants.GALLERY_SCREEN;
                    friendId = gPojo.getUserId();
                    setGalleryValues(gPojo);
                    btnSnd.setVisibility(View.GONE);
                    btnIgnore.setVisibility(View.GONE);
                    layoutDownloadDelete.setVisibility(View.VISIBLE);
                    tbPublicityLayout.setVisibility(View.VISIBLE);
                    Log.e(TAG, "onCreate: " + new Gson().toJson(gPojo));

                    if (gPojo.getMakePublic() == 1) {
                        tbPubilicity.setChecked(true);
                        tvPrivate.setVisibility(View.GONE);
                    } else {
                        tbPubilicity.setChecked(false);
                    }
                } else if (screenFrom == AppConstants.HOME_SCREEN) {
                    msgDetails = getIntent().getStringExtra(AppConstants.MESSAGE_DETAILS);
                    gPojo = new Gson().fromJson(msgDetails, GalleryPojo.class);
                    fromScreen = AppConstants.HOME_SCREEN;
                    setGalleryValues(gPojo);
                    tvPrivate.setVisibility(View.GONE);
                    layAction.setVisibility(View.GONE);
                } else if (screenFrom == AppConstants.EVENT_DETAILS_SCREEN) {
                    spannableString = new SpannableString("Event");
                    msgDetails = getIntent().getStringExtra(AppConstants.EVENT_DETAILS);
                    eventImagePojo = new Gson().fromJson(msgDetails, EventImage.class);
                    fromScreen = AppConstants.EVENT_DETAILS_SCREEN;
                    setGalleryValuesFromEvent(eventImagePojo);
                    tvPrivate.setVisibility(View.GONE);
                    if (getIntent().getBooleanExtra(AppConstants.HIDE_DELETE_BUTTON, false)) {
                        btnDelete.setVisibility(View.GONE);
                    }
                    btnIgnore.setVisibility(View.GONE);
                    btnSnd.setVisibility(View.GONE);
                    tbPublicityLayout.setVisibility(View.GONE);
                    layoutDownloadDelete.setVisibility(View.VISIBLE);
                } else {
                    msgDetails = getIntent().getStringExtra(AppConstants.MESSAGE_DETAILS);
                    model = new Gson().fromJson(msgDetails, RequestResponseModel.class);
                    fromScreen = AppConstants.REQUEST_SCREEN;
                    if (model.getType() == AppConstants.REQUEST_TYPE) {
                        mRequsted = true;
                        spannableString = new SpannableString("Request Received");
                        btnIgnore.setVisibility(View.VISIBLE);
                        requestResponse = true;
                    } else if (model.getType() == AppConstants.INITIATOR_TYPE) {
                        spannableString = new SpannableString("Request Sent");
                        btnIgnore.setVisibility(View.GONE);
                        layoutDownloadDelete.setVisibility(View.VISIBLE);
                        btnDownload.setVisibility(View.GONE);
                    } else {
                        spannableString = new SpannableString("Response");
                        btnIgnore.setVisibility(View.GONE);
                        layoutDownloadDelete.setVisibility(View.VISIBLE);
                        requestResponse = true;
                        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                        map.put(AppConstant.RES_UID, model.getUsers().get(0).getUserId());
                        map.put(AppConstant.R_MID, model.getMessageId().toString());
                        viewModel.seenResponse(userPojo.getToken(), map);
                        viewModel.seenData.observe(this, seenObserver);
                    }
                    setValues(model, fromScreen);
                }
            }
        }

        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tbPublicityView.setOnClickListener(v -> {
            if (tbPubilicity.isChecked()) {
                LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                if (model != null) {
                    map.put(AppConstant.RES_UID, model.getUsers().get(0).getUserId());
                    map.put(AppConstant.R_MID, model.getMessageId());
                }
                if (gPojo != null) {
                    map.put(AppConstant.RES_UID, gPojo.getUserId());
                    map.put(AppConstant.R_MID, gPojo.getMessageId());
                }
                if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this)) {
                    viewModel.makePrivate(userPojo.getToken(), map);
                    utilsFunctions.showDialog(GalleryViewActivity.this);
                    viewModel.makePrivateData.observe(this, privateObs);
                }
            } else {
                LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                if (model != null) {
                    map.put(AppConstant.RES_UID, model.getUsers().get(0).getUserId());
                    map.put(AppConstant.R_MID, model.getMessageId());
                }
                if (gPojo != null) {
                    map.put(AppConstant.RES_UID, gPojo.getUserId());
                    map.put(AppConstant.R_MID, gPojo.getMessageId());
                }
                if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this)) {
                    viewModel.makePublic(userPojo.getToken(), map);
                    utilsFunctions.showDialog(this);
                    viewModel.makePublicData.observe(this, publicObs);
                }
            }
        });
    }

    Observer<Resource<ApiResponseModel>> seenObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    AppConstants.GALLERY_UPDATE = true;
                    AppConstants.REQUEST_RESPONSE_UPDATE = true;
                    Log.e(TAG, "onChanged: seen Response" + (apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : null));
                    break;
            }
        }
    };


    private void setGalleryValues(GalleryPojo gPojo) {
        tvMsg.setText(gPojo.getMessage());
        tvUser.setText(getString(R.string.shared_by) + " " + gPojo.getUserName());
        tvDate.setText(gPojo.getDateTime());
        if (gPojo.getMediaURL() != null && !gPojo.getMediaURL().isEmpty()) {
            if (UtilsFunctions.getFileType(gPojo.getMediaURL()).equalsIgnoreCase(AppConstants.PICTURES)) {
                videoView.setVisibility(View.GONE);
                imgThumb.setVisibility(View.VISIBLE);
                imgThumb.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ImagePreviewActivity.class);
                    intent.putExtra(AppConstants.MEDIA, gPojo.getMediaURL());
                    startActivity(intent);
                });
                Glide.with(this)
                        .load(gPojo.getMediaURL())
                        .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                        .error(R.drawable.ic_landscape)
                        .into(imgThumb);
            }
            if (UtilsFunctions.getFileType(gPojo.getMediaURL()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                videoView.setVisibility(View.VISIBLE);
                imgThumb.setVisibility(View.GONE);
                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(
                                Uri.parse(gPojo.getMediaURL())));
                mPlayer.setMediaSource(mediaSource);
                mPlayer.prepare();
            }

        } else {
            videoView.setVisibility(View.GONE);
            imgThumb.setVisibility(View.VISIBLE);
        }
        btnSnd.setText(R.string.make_public);
    }

    private void setGalleryValuesFromEvent(EventImage gPojo) {
        tvMsg.setText(gPojo.getMessage());
        String name = gPojo.getUserName() != null ? gPojo.getUserName() : " ";
        tvUser.setText(getString(R.string.shared_by) + " " + name);
        tvDate.setText(gPojo.getDatetime());
        if (gPojo.getImageUrl() != null && !gPojo.getImageUrl().isEmpty()) {
            if (UtilsFunctions.getFileType(gPojo.getImageUrl()).equalsIgnoreCase(AppConstants.PICTURES)) {
                videoView.setVisibility(View.GONE);
                imgThumb.setVisibility(View.VISIBLE);
                imgThumb.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ImagePreviewActivity.class);
                    intent.putExtra(AppConstants.MEDIA, gPojo.getImageUrl());
                    startActivity(intent);
                });
                Glide.with(this)
                        .load(gPojo.getImageUrl())
                        .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                        .error(R.drawable.ic_landscape)
                        .into(imgThumb);
            }
            if (UtilsFunctions.getFileType(gPojo.getImageUrl()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                videoView.setVisibility(View.VISIBLE);
                imgThumb.setVisibility(View.GONE);
                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(
                                Uri.parse(gPojo.getImageUrl())));
                mPlayer.setMediaSource(mediaSource);
                mPlayer.prepare();
            }

        } else {
            videoView.setVisibility(View.GONE);
            imgThumb.setVisibility(View.VISIBLE);
        }
        btnSnd.setText(R.string.make_public);
    }


    private void setValues(RequestResponseModel model, int screen) {
        tbPublicityLayout.setVisibility(View.GONE);
        tvMsg.setText(model.getMessage());
        if (screen == AppConstants.REQUEST_SCREEN) {
            for (int i = 0; i < model.getUsers().size(); i++) {
                requested_users = requested_users + model.getUsers().get(i).getUserName() + comma_separator;
            }
            requested_users = requested_users.substring(0, requested_users.length() - 2).trim();
        } else {
            requested_users = model.getUserName();
        }
        tvUser.setText(model.getType() == AppConstants.INITIATOR_TYPE ?
                getResources().getString(R.string.requested_to_label) + " " + requested_users
                : getResources().getString(R.string.shared_by) + " " + requested_users);
        tvDate.setText(model.getDateTime());

        if (screen == AppConstants.REQUEST_SCREEN) {
            friendId = model.getUsers().get(0).getUserId();
        } else {
            friendId = model.getUserId();
        }
        if (model.getMediaURL() != null && !model.getMediaURL().isEmpty()) {
            if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.PICTURES)) {
                videoView.setVisibility(View.GONE);
                imgThumb.setVisibility(View.VISIBLE);
                imgThumb.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ImagePreviewActivity.class);
                    intent.putExtra(AppConstants.MEDIA, model.getMediaURL());
                    startActivity(intent);
                });
                Glide.with(this)
                        .load(model.getMediaURL())
                        .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                        .error(R.drawable.ic_landscape)
                        .into(imgThumb);
            }
            if (UtilsFunctions.getFileType(model.getMediaURL()).equalsIgnoreCase(AppConstants.VIDEOS)) {
                videoView.setVisibility(View.VISIBLE);
                imgThumb.setVisibility(View.GONE);
                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(
                                Uri.parse(model.getMediaURL())));
                mPlayer.setMediaSource(mediaSource);
                mPlayer.prepare();
            }
        } else {
            videoView.setVisibility(View.INVISIBLE);
            imgThumb.setVisibility(View.GONE);
        }

        Log.e(TAG, "setValues: " + new Gson().toJson(model));

        if (model.getType() == AppConstants.REQUEST_TYPE)
            btnSnd.setText(R.string.respond_label);
        else if (model.getType() == AppConstants.INITIATOR_TYPE) {
            tbPublicityLayout.setVisibility(View.GONE);
            btnSnd.setVisibility(View.GONE);
        } else {
            tbPublicityLayout.setVisibility(View.VISIBLE);
            btnSnd.setVisibility(View.GONE);
            btnSnd.setText(R.string.make_public);
        }
    }


    @OnClick(R.id.btnSnd)
    void send() {
        if (mRequsted) {
            Intent i = new Intent(this, RequestResponseActivity.class);
            i.putExtra(AppConstants.ACTION, AppConstants.PICTURE);
            i.putExtra(AppConstants.TRANSACTION, AppConstants.RESPOND);
            i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetails);
            startActivity(i);
        } else {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            if (model != null) {
                map.put(AppConstant.RES_UID, model.getUserId());
                map.put(AppConstant.R_MID, model.getMessageId());
            }
            if (gPojo != null) {
                map.put(AppConstant.RES_UID, gPojo.getUserId());
                map.put(AppConstant.R_MID, gPojo.getMessageId());
            }
            viewModel.makePublic(userPojo.getToken(), map);
            utilsFunctions.showDialog(this);
            viewModel.makePublicData.observe(this, publicObs);
        }
    }

    @OnClick(R.id.btnIgnore)
    void ignoreRequest() {
        if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this))
            ignore();
    }

    private void deleteEventImageWithID(ArrayList<Integer> imageID) {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.EVENT_IMAGE_ID, imageID);
        eventViewModel.deleteEventImage(userAuthPojo.getToken().toString(), map);
        eventViewModel.deleteEventImageData().observe(this, eventImageDeleteObserver);
    }

    Observer<Resource<ApiResponseModel>> eventImageDeleteObserver = new Observer<Resource<ApiResponseModel>>() {

        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(GalleryViewActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    progressBar.setVisibility(View.GONE);
                    assert apiResponseModelResource.data != null;
                    UtilsFunctions.showToast(GalleryViewActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    setResult(Activity.RESULT_OK);
                    finish();
                    break;
            }
        }
    };

    @OnClick(R.id.btnDelete)
    void deleteRequest() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this)) {
                        if (fromScreen == AppConstants.EVENT_DETAILS_SCREEN) {
                            progressBar.setVisibility(View.VISIBLE);
                            ArrayList<Integer> imageId = new ArrayList<>();
                            imageId.add(eventImagePojo.getImageId());
                            deleteEventImageWithID(imageId);
                        } else {
                            delete(gPojo != null ? String.valueOf(AppConstants.RESPONSE_TYPE)
                                    : model.getType().toString());
                        }
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        UtilsFunctions.showConfirmationDialog(this,
                getString(R.string.delete_confirmation_message), getString(R.string.yes),
                getString(R.string.no), dialogClickListener);
    }

    private void ignore() {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.RES_UID, userPojo.getId().toString());
        map.put(AppConstant.M_ID, model.getMessageId());
        utilsFunctions.showDialog(this);
        viewModel.ignoreRequest(userPojo.getToken(), map);
        viewModel.getIgnoreData().observe(this, ignoreObs);
    }

    private void delete(String type) {
        utilsFunctions.showDialog(this);
        // TODO: Need to pass the type from gPojo after API updates
        viewModel.deleteRequest(userPojo.getToken(), type, gPojo != null ?
                gPojo.getMessageId().toString() : model.getMessageId().toString());
        viewModel.getDeleteData().observe(this, deleteObs);
    }

    private void checkPermission(String url) {
        if (UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)) {
            startDownload(url);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_STORE}, STORE_PERMISSION);
        }
    }

    @OnClick(R.id.btnDownload)
    void performDownloadAction() {
        if (eventImagePojo != null) {
            url = eventImagePojo.getImageUrl();
        } else if (gPojo != null) {
            url = gPojo.getMediaURL();
        } else {
            url = model.getMediaURL();
        }
        if (url != null) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                checkPermission(url);
            } else {
                startDownload(url);
            }
        } else {
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.no_data));
        }
    }

    void startDownload(String url) {
        UtilsFunctions.startDownload(url, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            startDownload(url);
            LogCapture.e(TAG, "Permission Granted");
        } else {
            LogCapture.e(TAG, "Permission Denied");
        }
    }

    Observer<Resource<ApiResponseModel>> publicObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    AppConstants.GALLERY_UPDATE = true;
                    AppConstants.FEED_UPDATE = true;
                    if (gPojo != null)
                        gPojo.setMakePublic(1);
                    if (model != null)
                        model.setMakePublic(1);
                    tvPrivate.setVisibility(View.GONE);
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast,
                            apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : "Response made public!");
                    Log.e(TAG, "onChanged: ignore Response" + (apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : null));
                    GalleryViewActivity.this.finish();
                    break;
            }
        }
    };


    Observer<Resource<ApiResponseModel>> privateObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    AppConstants.GALLERY_UPDATE = true;
                    AppConstants.FEED_UPDATE = true;
                    gPojo.setMakePublic(0);
                    tvPrivate.setVisibility(View.VISIBLE);
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast,
                            apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : "Response made private!");
                    Log.e(TAG, "onChanged: ignore Response" + (apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : null));
                    GalleryViewActivity.this.finish();
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> ignoreObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    AppConstants.REQUEST_RESPONSE_UPDATE = true;
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : "Request ignored!");
                    Log.e(TAG, "onChanged: ignore Response" + (apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : null));
                    GalleryViewActivity.this.finish();
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> deleteObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast,
                            apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    AppConstants.REQUEST_RESPONSE_UPDATE = true;
                    AppConstants.GALLERY_UPDATE = true;
                    AppConstants.FEED_UPDATE = true;
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast,
                            getString(R.string.msg_deleted));
                    Log.e(TAG, "onChanged: delete Response " + (apiResponseModelResource.data != null ?
                            apiResponseModelResource.data.getMessage() : null));
                    GalleryViewActivity.this.finish();
                    break;
            }
        }
    };


  /*  public void showRespondSheet() {
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
            Intent i = new Intent(this, RequestResponseActivity.class);
            i.putExtra(AppConstants.ACTION, AppConstants.PICTURE);
            i.putExtra(AppConstants.TRANSACTION, AppConstants.RESPOND);
            i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetails);
            startActivity(i);
            dialog.dismiss();
        });

        vVideo.setOnClickListener(v -> {
            Intent i = new Intent(this, RequestResponseActivity.class);
            i.putExtra(AppConstants.ACTION, AppConstants.VIDEO);
            i.putExtra(AppConstants.TRANSACTION, AppConstants.RESPOND);
            i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetails);
            startActivity(i);
            dialog.dismiss();
        });


        dialog.show();
    }*/

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_response, menu);

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem mAddFriend = menu.findItem(R.id.mAddFriend);

        if (requestResponse)
            mAddFriend.setVisible(true);
        else {
            mAddFriend.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.mAddFriend:
                if (UtilsFunctions.isNetworkAvail(GalleryViewActivity.this))
                    addFriend();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void addFriend() {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();


        map.put(AppConstant.USR_ID, userPojo.getId());
        map.put(AppConstant.FRIEND_ID, friendId);
        utilsFunctions.showDialog(this);
        friendsViewModel.sendFriendRequest(userPojo.getToken(), map);
        friendsViewModel.getRequestData().observe(this, new Observer<Resource<ApiResponseModel>>() {
            @Override
            public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
                switch (apiResponseModelResource.status) {
                    case ERROR:
                        utilsFunctions.hideDialog();
                        UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                        break;
                    case SUCCESS:
                        utilsFunctions.hideDialog();
                        UtilsFunctions.showToast(GalleryViewActivity.this, AppConstants.shortToast, apiResponseModelResource.data != null ? apiResponseModelResource.data.getMessage() : "Request ignored!");
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }
}
