package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.CAMERA;
import static com.picmob.android.utils.AppConstants.CAMERA_REQUEST;
import static com.picmob.android.utils.AppConstants.GALLERY_REQUEST;
import static com.picmob.android.utils.AppConstants.READ_STORE;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.cm.pc.Country;
import com.cm.pc.CountryPicker;
import com.cm.pc.listeners.OnCountryPickerListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.implementation.BlobStorageService;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.profile.ProfilePojo;
import com.picmob.android.mvvm.profile.ProfileViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.FileUtils;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.picmob.android.utils.widgets.ShapeImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends BaseActivity implements com.picmob.android.listeners.BlobStorageService {

    @BindView(R.id.imgProfile)
    ShapeImageView imgProfile;
    @BindView(R.id.layEditImage)
    RelativeLayout layEditImage;
    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.tvFirstName)
    TextView tvFirstName;
    @BindView(R.id.tvLastName)
    TextView tvLastName;
    @BindView(R.id.tvEmail)
    EditText tvEmail;
    @BindView(R.id.tvPhone)
    EditText tvPhone;
    @BindView(R.id.tvZip)
    EditText tvZip;
    @BindView(R.id.tvChangeLocation)
    TextView tvChangeLocation;
    @BindView(R.id.imgDrop)
    ImageView imgDrop;
    @BindView(R.id.layCC)
    LinearLayout layCC;
    @BindView(R.id.tvCC)
    TextView tvCC;

    private String mLocation;
    private ProfileViewModel viewModel;
    private UserAuthPojo pojo;
    private static final String TAG = "ProfileActivity";
    private Uri outUri;
    private UtilsFunctions utilsFunctions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        SpannableString spannableString = new SpannableString("Profile");
        General general = new General(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        if (UtilsFunctions.isNetworkAvail(this)) {
            viewModel.getUserDetails(pojo.getToken(), pojo.getId().toString());
            viewModel.getProfileData().observe(this, profileObserver);
        }
        utilsFunctions = new UtilsFunctions();
        disableEdits();
    }

    Observer<Resource<ProfilePojo>> profileObserver = new Observer<Resource<ProfilePojo>>() {
        @Override
        public void onChanged(Resource<ProfilePojo> profilePojoResource) {
            switch (profilePojoResource.status) {

                case ERROR:
                    UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, profilePojoResource.message);
                    break;
                case SUCCESS:
                    setUserDetails(profilePojoResource.data);
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> locationObserver = apiResponseModelResource -> {
        switch (apiResponseModelResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                break;
            case SUCCESS:
                tvZip.setText(mLocation);
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                break;
        }
    };

    Observer<Resource<ApiResponseModel>> updateUser = apiResponseModelResource -> {
        switch (apiResponseModelResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                break;
            case SUCCESS:
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                break;
        }
    };

    Observer<Resource<ApiResponseModel>> imageObserver = apiResponseModelResource -> {
        switch (apiResponseModelResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                break;
            case SUCCESS:
                utilsFunctions.hideDialog();
                Glide.with(ProfileActivity.this).load(outUri).centerCrop().placeholder(R.drawable.ic_user).into(imgProfile);
                UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                break;
        }
    };

    @OnClick(R.id.layCC)
    void showCC() {
        CountryPicker countryPicker =
                new CountryPicker.Builder().with(this)
                        .listener(new OnCountryPickerListener() {
                            @Override
                            public void onSelectCountry(Country country) {
                                tvCC.setText(country.getDialCode());
                            }
                        }).style(R.style.Theme_Design_BottomSheetDialog)
                        .canSearch(true)
                        .sortBy(CountryPicker.SORT_BY_NAME)
                        .theme(CountryPicker.THEME_NEW).build();

        countryPicker.showBottomSheet(this);
    }

    private void setUserDetails(ProfilePojo data) {
        tvUserName.setText(data.getUsername());
        tvFirstName.setText(data.getFirstName());
        tvLastName.setText(data.getLastName());
        tvEmail.setText(data.getEmail());
        tvPhone.setText(data.getPhoneNumber());
        tvCC.setText(data.getPhoneCode());

        if (data.getLocation().contains(";")) {
            List<String> strings = Arrays.asList(data.getLocation().split(";"));
            String location = strings.get(0);
            tvZip.setText(location);
        } else {
            tvZip.setText(data.getLocation());
        }

        if (data.getAvatarURL() != null && !data.getAvatarURL().isEmpty()) {
            Glide.with(this)
                    .load(data.getAvatarURL())
                    .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                    .error(R.drawable.ic_user)
                    .into(imgProfile);
        } else
            Glide.with(this)
                    .load(R.drawable.ic_user)
                    .placeholder(UtilsFunctions.getCircularProgressDrawable(this))
                    .error(R.drawable.ic_user)
                    .into(imgProfile);
    }


    @OnClick(R.id.tvChangeLocation)
    void changeLocation() {
        showLocationSheet();
    }

    @OnClick(R.id.layEditImage)
    void changeImage() {
        checkPermissionAndOpenSheet();
    }

    private void checkPermissionAndOpenSheet() {
        if (UtilsFunctions.hasMultiplePermissions(this, READ_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, CAMERA)) {
            showImageSheet();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_STORE, WRITE_STORE, CAMERA}, STORE_PERMISSION);
        }
    }

    private void showImageSheet() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_image, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        LinearLayout vCam = dialogView.findViewById(R.id.vCam);
        LinearLayout vImg = dialogView.findViewById(R.id.vImg);

        vImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGalleryImage();
                dialog.dismiss();
            }
        });
        vCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCapture();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getGalleryImage() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY_REQUEST);
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

                outUri = FileProvider.getUriForFile(this, AppConstants.FILE_PROVIDER, photoFile);

                takePictureIntent.putExtra("output", outUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }


    private void showLocationSheet() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_profile, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        etLocation.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!UtilsFunctions.checkPhone(etLocation.getText().toString()))
                        UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, getString(R.string.invalid_zip));
                    else {

                        utilsFunctions.showDialog(ProfileActivity.this);

                        mLocation = etLocation.getText().toString();
                        LinkedTreeMap<String, String> location = new LinkedTreeMap<>();
                        location.put(AppConstant.LOCATION, mLocation);

                        viewModel.updateLocation(pojo.getToken(), pojo.getId().toString(), location);
                        viewModel.getLocationData().observe(ProfileActivity.this, locationObserver);
                        dialog.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UtilsFunctions.checkPhone(etLocation.getText().toString()))
                    UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, getString(R.string.invalid_zip));
                else {
                    utilsFunctions.showDialog(ProfileActivity.this);
                    mLocation = etLocation.getText().toString();

                    LinkedTreeMap<String, String> location = new LinkedTreeMap<>();
                    location.put(AppConstant.LOCATION, mLocation);

                    viewModel.updateLocation(pojo.getToken(), pojo.getId().toString(), location);

                    viewModel.getLocationData().observe(ProfileActivity.this, locationObserver);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                outUri = data != null ? Objects.requireNonNull(data).getData() : null;
                Log.e(TAG, "onActivityResult: " + Objects.requireNonNull(outUri).toString());
                try {
                    utilsFunctions.showDialog(ProfileActivity.this);
                    Glide.with(this).load(outUri).error(R.drawable.ic_user).into(imgProfile);
                    new BlobStorageService(this).uploadFile(FileUtils.getFileFromUri(this, outUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                utilsFunctions.showDialog(ProfileActivity.this);
                Glide.with(this).load(outUri).error(R.drawable.ic_user).into(imgProfile);
//                Glide.with(ProfileActivity.this).load(outUri).centerCrop().placeholder(R.drawable.ic_user).into(imgProfile);
                new BlobStorageService(this).uploadFile(new File(AppConstants.mPhotoPath));
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem mEdit = menu.findItem(R.id.mEdit);

        MenuItem mUpload = menu.findItem(R.id.mUpload);
        mUpload.setVisible(false);

        mEdit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (UtilsFunctions.isNetworkAvail(ProfileActivity.this)) {
                    mEdit.setVisible(false);
                    mUpload.setVisible(true);
                    enableEdits();
                }

                return false;
            }
        });

        mUpload.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (UtilsFunctions.isNetworkAvail(ProfileActivity.this)) {
                    updateData(mUpload, mEdit);
                }
                return false;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void updateData(MenuItem mUpload, MenuItem mEdit) {

        if (!UtilsFunctions.checkName(tvFirstName.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Fname));

        else if (!UtilsFunctions.checkName(tvLastName.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Lname));

        else if (!UtilsFunctions.checkEmail(tvEmail.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Email));

        else if (!UtilsFunctions.checkPhone(tvPhone.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Phone));

        else if (!UtilsFunctions.checkPhone(tvZip.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_zip));

        else {
            mUpload.setVisible(false);
            mEdit.setVisible(true);
            disableEdits();
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.F_NAME, tvFirstName.getText().toString());
            map.put(AppConstant.L_NAME, tvLastName.getText().toString());
            map.put(AppConstant.PHONE, tvPhone.getText().toString());
            map.put(AppConstant.EMAIL, tvEmail.getText().toString());
            map.put(AppConstant.LOCATION, tvZip.getText().toString());
            map.put(AppConstant.PHONE_CODE, tvCC.getText().toString());
            utilsFunctions.showDialog(this);
            viewModel.updateUserData(pojo.getToken(), pojo.getId().toString(), map);
            viewModel.getUpdateProfileData().observe(ProfileActivity.this,
                    updateUser);
        }
    }


    private void disableEdits() {

        int color = Color.parseColor("#333333");
        tvFirstName.setTextColor(color);
        tvLastName.setTextColor(color);
        tvUserName.setTextColor(color);
        tvEmail.setTextColor(color);
        tvZip.setTextColor(color);
        tvPhone.setTextColor(color);
        imgDrop.setVisibility(View.GONE);
        layCC.setEnabled(false);
        tvFirstName.setEnabled(true);
        tvLastName.setEnabled(true);
        tvEmail.setEnabled(false);
        tvZip.setEnabled(false);
        tvPhone.setEnabled(false);
    }

    private void enableEdits() {
        int color = Color.parseColor("#8F999999");
        tvUserName.setTextColor(color);
        imgDrop.setVisibility(View.VISIBLE);
        layCC.setEnabled(true);
        tvFirstName.setEnabled(true);
        tvLastName.setEnabled(true);
        tvEmail.setEnabled(true);
        tvZip.setEnabled(true);
        tvPhone.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void getUrl(URI url) {
        ProfileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinkedTreeMap<String, String> image = new LinkedTreeMap<>();
                image.put(AppConstant.IMAGE_URL, url.toString());
                viewModel.updateImage(pojo.getToken(), pojo.getId().toString(), image);
                viewModel.getImageData().observe(ProfileActivity.this, imageObserver);
            }
        });

    }

    @Override
    public void error(String error) {
        utilsFunctions.hideDialog();
        UtilsFunctions.showToast(ProfileActivity.this, AppConstants.shortToast, error);
    }
}
