package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.CAMERA;
import static com.picmob.android.utils.AppConstants.CAMERA_REQUEST;
import static com.picmob.android.utils.AppConstants.GALLERY_REQUEST;
import static com.picmob.android.utils.AppConstants.READ_STORE;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.picmob.android.R;
import com.picmob.android.implementation.BlobStorageService;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.events.CreateOrUpdateEventPojo;
import com.picmob.android.mvvm.events.EventUsers;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.events.PhonebookUsers;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.FileUtils;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateEventActivity extends BaseActivity implements com.picmob.android.listeners.BlobStorageService {
    private static final String TAG = "CreateEventActivity";
    @BindView(R.id.event_name)
    EditText eventName;
    @BindView(R.id.event_msg)
    EditText eventDescription;
    @BindView(R.id.img_thumb)
    ImageView imgThumb;
    @BindView(R.id.event_type_group)
    RadioGroup eventTypeGroup;
    @BindView(R.id.button_continue)
    Button continueCreateEvent;
    @BindView(R.id.select_image)
    Button selectImage;
    RadioButton eventTypeButton;
    String eventTypeSelected;
    UtilsFunctions utilsFunctions;
    String displayImage;
    String eventLongitude;
    String eventLatitude;
    private UserAuthPojo userAuthPojo;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.create_card_view)
    CardView createCardView;
    Bundle extras;
    EventViewModel eventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        utilsFunctions = new UtilsFunctions();
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        extras = getIntent().getExtras();
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        SpannableString spannableString = new SpannableString(getString(R.string.create_event_label));
        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        continueCreateEvent.setText(getResources().getString(R.string.continues));
        eventTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedId = eventTypeGroup.getCheckedRadioButtonId();
                eventTypeButton = (RadioButton) findViewById(selectedId);
                if (eventTypeButton.getText().toString().equals(getResources().getString(R.string.private_label))) {
                    continueCreateEvent.setText(getResources().getString(R.string.continues));
                } else {
                    continueCreateEvent.setText(getResources().getString(R.string.create_event));
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    super.onBackPressed();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        UtilsFunctions.showConfirmationDialog(this,
                getString(R.string.event_back_confirmation_message), getString(R.string.yes),
                getString(R.string.no), dialogClickListener);
    }

    @OnClick(R.id.select_image)
    void selectImage() {
        checkPermissionAndOpenSheet();
    }


    @OnClick(R.id.button_continue)
    void createEvent() {
        if (isValidate()) {
            if (eventTypeButton == null) {
                int selectedId = eventTypeGroup.getCheckedRadioButtonId();
                eventTypeButton = (RadioButton) findViewById(selectedId);
            }
            if (eventTypeButton.getText().toString().equals(getResources().getString(R.string.private_label))) {
                eventTypeSelected = AppConstants.PRIVATE_TYPE_LABEL;
            } else {
                eventTypeSelected = AppConstants.PUBLIC_TYPE_LABEL;
            }
            if (extras != null) {
                eventLatitude = String.valueOf(extras.getDouble("LATITUDE"));
                eventLongitude = String.valueOf(extras.getDouble("LONGITUDE"));
            }

            if (eventTypeSelected.equals((getResources().getString(R.string.private_label)))) {
                Intent i = new Intent(CreateEventActivity.this, EventUserSelectActivity.class);
                i.putExtra("USER_ID", userAuthPojo.getId());
                i.putExtra("EVENT_TYPE_ID", eventTypeSelected);
                i.putExtra("EVENT_NAME", eventName.getText().toString());
                i.putExtra("EVENT_DESCRIPTION", eventDescription.getText().toString());
                i.putExtra("EVENT_IMAGE", displayImage);
                i.putExtra("LATITUDE", eventLatitude);
                i.putExtra("LONGITUDE", eventLongitude);
                startActivity(i);
            } else {
                utilsFunctions.showDialog(CreateEventActivity.this);
                try {
                    if (displayImage != null) {
                        if (AppConstants.mPhotoPath != null) {
                            new BlobStorageService(this).uploadFile(new File(displayImage));
                        } else {
                            Uri eventImageURI = Uri.parse(displayImage);
                            new BlobStorageService(this)
                                    .uploadFile(FileUtils.getFileFromUri(this, eventImageURI));
                        }
                    } else {
                        sendEventInvitation();
                        utilsFunctions.hideDialog();
                    }
                } catch (Exception e) {
                    utilsFunctions.hideDialog();
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isValidate() {
        if (eventName.getText().toString().trim().length() == 0) {
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.event_name_empty));
            return false;
        } else if (eventDescription.getText().toString().trim().length() == 0) {
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.event_description_empty));
            return false;
        } else return true;
    }

    private void checkPermissionAndOpenSheet() {
        if (UtilsFunctions.hasMultiplePermissions(this, READ_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, CAMERA)) {
            showFileUploadSheet();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_STORE, WRITE_STORE, CAMERA}, STORE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            checkPermissionAndOpenSheet();
            LogCapture.e(TAG, "Permission Granted, Now you can use local drive .CONFIRMED");
        } else {
            LogCapture.e(TAG, "Permission Denied, You cannot use local drive .");
        }
    }

    private void showFileUploadSheet() {
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
            fileAction(CAMERA_REQUEST);
            dialog.dismiss();
        });

        vImg.setOnClickListener(v -> {
            fileAction(GALLERY_REQUEST);
            dialog.dismiss();
        });
        dialog.show();
    }


    private void cameraCapture() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = UtilsFunctions.createImageFile(this);
            } catch (IOException ex) {
                LogCapture.e(TAG, "takeCameraPicture:" + ex.getLocalizedMessage());
            }
            if (photoFile != null) {
                takePictureIntent.putExtra("output", FileProvider.getUriForFile(this,
                        AppConstants.FILE_PROVIDER, photoFile));
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private void getGalleryImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY_REQUEST);
    }

    private void fileAction(int action) {
        switch (action) {
            case CAMERA_REQUEST:
                cameraCapture();
                break;
            case GALLERY_REQUEST:
                getGalleryImage();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            createCardView.setVisibility(View.GONE);
            continueCreateEvent.setVisibility(View.GONE);
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data != null ? Objects.requireNonNull(data).getData() : null;
                Log.e(TAG, "onActivityResult: " + selectedImage.toString());
                try {
                    displayImage = selectedImage.toString();
                    Glide.with(CreateEventActivity.this).load(selectedImage)
                            .placeholder(R.drawable.ic_landscape).fitCenter().into(imgThumb);
                    progressBar.setVisibility(View.GONE);
                    createCardView.setVisibility(View.VISIBLE);
                    continueCreateEvent.setVisibility(View.VISIBLE);
                    utilsFunctions.hideDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                try {
                    displayImage = AppConstants.mPhotoPath;
                    Glide.with(CreateEventActivity.this).load(AppConstants.mPhotoPath)
                            .placeholder(R.drawable.ic_landscape).into(imgThumb);
                    progressBar.setVisibility(View.GONE);
                    createCardView.setVisibility(View.VISIBLE);
                    continueCreateEvent.setVisibility(View.VISIBLE);
                    utilsFunctions.hideDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendEventInvitation() {
        ArrayList<EventUsers> vicinityList = new ArrayList<>();
        ArrayList<EventUsers> registeredList = new ArrayList<>();
        ArrayList<PhonebookUsers> contactList = new ArrayList<>();
        CreateOrUpdateEventPojo createOrUpdateEventPojo =
                new CreateOrUpdateEventPojo(0, userAuthPojo.getId(), eventName.getText().toString(),
                        eventDescription.getText().toString(), displayImage, eventTypeSelected, eventLatitude,
                        eventLongitude,
                        vicinityList, registeredList, contactList);
        if (UtilsFunctions.isNetworkAvail(CreateEventActivity.this)) {
            utilsFunctions.showDialog(this);
            sendEvent(createOrUpdateEventPojo, false);
            utilsFunctions.hideDialog();
        }
    }

    private void sendEvent(CreateOrUpdateEventPojo createOrUpdateEventPojo, boolean isUpdate) {
        eventViewModel.sendEvent(userAuthPojo.getToken().toString(), createOrUpdateEventPojo, isUpdate);
        eventViewModel.createEventData().observe(this, eventCreateObserver);
    }

    Observer<Resource<ApiResponseModel>> eventCreateObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(CreateEventActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(CreateEventActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    AppConstants.EVENT_LIST_UPDATE = true;
                    Intent i = new Intent(CreateEventActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };


    @Override
    public void getUrl(URI url) {
        displayImage = url.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendEventInvitation();
            }
        });
    }

    @Override
    public void error(String error) {
        UtilsFunctions.showToast(CreateEventActivity.this, AppConstants.shortToast, error);
        utilsFunctions.hideDialog();
    }

}