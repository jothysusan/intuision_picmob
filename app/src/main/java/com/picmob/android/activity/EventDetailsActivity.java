package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.adapters.EventDetailsAdapter;
import com.picmob.android.listeners.EventItemSelectionListener;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.events.CreateOrUpdateEventPojo;
import com.picmob.android.mvvm.events.EventDetailsPojo;
import com.picmob.android.mvvm.events.EventImage;
import com.picmob.android.mvvm.events.EventImageListener;
import com.picmob.android.mvvm.events.EventUsers;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.events.PhonebookUsers;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventDetailsActivity extends BaseActivity implements EventImageListener, EventItemSelectionListener {

    @BindView(R.id.details_card_view)
    CardView detailsCardView;
    @BindView(R.id.hostedByLabel)
    TextView hostedBy;
    @BindView(R.id.tbPublicity)
    ToggleButton tbPublicity;
    @BindView(R.id.toggleClickView)
    View toggleClickView;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.btnAddMedia)
    Button addMedia;
    @BindView(R.id.eventRecyclerView)
    RecyclerView eventRecyclerView;
    @BindView(R.id.btnDownloadSelected)
    Button btnDownloadSelected;
    @BindView(R.id.btnDeleteSelected)
    Button btnDeleteSelected;
    @BindView(R.id.btnDelete)
    Button btnDelete;
    @BindView(R.id.btnAddRemoveUser)
    Button btnAddRemoveUser;
    @BindView(R.id.selectedActionLayout)
    LinearLayout selectedActionLayout;
    @BindView(R.id.bottomLayout)
    ConstraintLayout bottomLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.edit_description)
    ImageView editDescription;
    private EventDetailsPojo eventDetails;
    private static final String TAG = "EventDetailsActivity";
    ArrayList<EventImage> gList = new ArrayList<>();
    private UserAuthPojo userAuthPojo;
    private EventDetailsAdapter mEventImagesAdapter;
    private List<EventImage> selectedItems;
    UtilsFunctions utilsFunctions;
    EventViewModel eventViewModel;
    String eventTypeSelected;
    General general;
    SpannableString spannableString;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    int eventId;
    String updatedTitleOrDescription;
    boolean isTitle = false;
    MenuItem mEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);
        spannableString = new SpannableString(getString(R.string.event));
        utilsFunctions = new UtilsFunctions();
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        progressBar.setVisibility(View.VISIBLE);
        toggleClickView.setVisibility(View.GONE);
        detailsCardView.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        description.setEnabled(false);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(AppConstant.EVENT_ID)) {
                eventId = getIntent().getIntExtra(AppConstant.EVENT_ID, 0);
            }
        }
        general = new General(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        mEventImagesAdapter = new EventDetailsAdapter(this, this, gList, this);
        eventRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        eventRecyclerView.setAdapter(mEventImagesAdapter);
        toggleClickView.setOnClickListener(v -> {
            if (tbPublicity.isChecked()) {
                tbPublicity.setChecked(false);
            } else {
                tbPublicity.setChecked(true);
            }
            doMakePublicORPrivateAction(tbPublicity.getText().toString());
        });
        tbPublicity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hostedBy.setText(getResources().getQuantityString(R.plurals.hosted_by,
                    eventDetails.getHostId() == userAuthPojo.getId() ? 1 : 2,
                    eventDetails.getType(),
                    eventDetails.getHostName()));
            if (isChecked) {
                btnAddRemoveUser.setVisibility(View.GONE);
            } else {
                btnAddRemoveUser.setVisibility(View.VISIBLE);
            }
        });
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            progressBar.setVisibility(View.VISIBLE);
                            getEventDetails(eventDetails.getId());
                        }
                    }
                });
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogButtonClicked(v);
            }
        });
        editDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogButtonClicked(v);
            }
        });
    }

    public void showAlertDialogButtonClicked(View view) {
        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        if (view instanceof TextView) {
            isTitle = false;
        } else isTitle = !(view instanceof ImageView);
        builder.setTitle(isTitle ? getResources().getString(R.string.event_name_label)
                : getResources().getString(R.string.description_label));
        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.edit_dialog_box,
                        null);
        builder.setView(customLayout);
        EditText editText
                = customLayout
                .findViewById(
                        R.id.edit_title_description);
        editText.setHint(isTitle ? getResources().getString(R.string.enter_event_name_label)
                : getResources().getString(R.string.enter_event_description_label));
        editText.setText(isTitle ? eventDetails.getName() : eventDetails.getDescription());
        // add a button
        builder
                .setPositiveButton(
                        getResources().getString(R.string.submit),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                // send data from the
                                // AlertDialog to the Activity

                                updatedTitleOrDescription = editText.getText().toString();
                                if (updatedTitleOrDescription.equals("")) {
                                    UtilsFunctions.showToast(EventDetailsActivity.this,
                                            AppConstants.shortToast,
                                            getResources().getString(R.string.event_description_empty));
                                } else {
                                    updateTitleOrDescription();
                                }
                            }
                        });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }

    private void updateTitleOrDescription() {
        if (selectedItems != null)
            selectedItems.clear();
        progressBar.setVisibility(View.VISIBLE);
        toggleClickView.setVisibility(View.GONE);
        detailsCardView.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        ArrayList<EventUsers> vicinityList = new ArrayList<>();
        ArrayList<EventUsers> registeredList = new ArrayList<>();
        ArrayList<PhonebookUsers> contactList = new ArrayList<>();
        if (eventTypeSelected == null)
            eventTypeSelected = eventDetails.getType();
        callUpdateEvent(vicinityList, registeredList, contactList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        toggleClickView.setVisibility(View.GONE);
        detailsCardView.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        selectedActionLayout.setVisibility(View.GONE);
        getEventDetails(eventId);
    }

    private void doMakePublicORPrivateAction(String toggleButtonText) {
        if (getResources().getString(R.string.make_public).equals(toggleButtonText)) {
            eventTypeSelected = AppConstants.PRIVATE_TYPE_LABEL;
        } else {
            eventTypeSelected = AppConstants.PUBLIC_TYPE_LABEL;
        }
        makePublicOrPrivate();
    }


    private void makePublicOrPrivate() {
        if (selectedItems != null)
            selectedItems.clear();
        progressBar.setVisibility(View.VISIBLE);
        toggleClickView.setVisibility(View.GONE);
        detailsCardView.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        ArrayList<EventUsers> vicinityList = new ArrayList<>();
        ArrayList<EventUsers> registeredList = new ArrayList<>();
        ArrayList<PhonebookUsers> contactList = new ArrayList<>();
        if (eventTypeSelected.equals(AppConstants.PRIVATE_TYPE_LABEL)) {
            if (eventDetails.getNearyby_users().size() > 0
                    || eventDetails.getPhonebook_users().size() > 0 ||
                    eventDetails.getRegistered_users().size() > 0) {
                vicinityList.addAll(eventDetails.getNearyby_users());
                registeredList.addAll(eventDetails.getRegistered_users());
                contactList.addAll(eventDetails.getPhonebook_users());
                callUpdateEvent(vicinityList, registeredList, contactList);
            } else {
                Intent i = new Intent(EventDetailsActivity.this, EventUserSelectActivity.class);
                i.putExtra(AppConstant.EVENT_ID, eventDetails.getId());
                i.putExtra("USER_ID", userAuthPojo.getId());
                i.putExtra("EVENT_TYPE_ID", eventTypeSelected);
                i.putExtra("EVENT_VICINITY", eventDetails.getNearyby_users());
                i.putExtra("EVENT_REGISTERED", eventDetails.getRegistered_users());
                i.putExtra("EVENT_PHONE_NUMBER", eventDetails.getPhonebook_users());
                i.putExtra("LATITUDE", eventDetails.getLatitude());
                i.putExtra("LONGITUDE", eventDetails.getLongitude());
                someActivityResultLauncher.launch(i);
            }
        } else {
            callUpdateEvent(vicinityList, registeredList, contactList);
        }
    }

    private void callUpdateEvent(ArrayList<EventUsers> vicinityList, ArrayList<EventUsers> registeredList,
                                 ArrayList<PhonebookUsers> contactList) {
        CreateOrUpdateEventPojo createOrUpdateEventPojo =
                new CreateOrUpdateEventPojo(eventDetails.getId(), userAuthPojo.getId(),
                        isTitle ? updatedTitleOrDescription : "",
                        isTitle ? "" : updatedTitleOrDescription, "",
                        eventTypeSelected, "",
                        "",
                        vicinityList, registeredList, contactList);
        if (UtilsFunctions.isNetworkAvail(EventDetailsActivity.this)) {
            utilsFunctions.showDialog(this);
            updateEvent(createOrUpdateEventPojo, true);
            utilsFunctions.hideDialog();
        }
    }

    private void updateEvent(CreateOrUpdateEventPojo createOrUpdateEventPojo, boolean isUpdate) {
        eventViewModel.sendEvent(userAuthPojo.getToken().toString(), createOrUpdateEventPojo, isUpdate);
        eventViewModel.createEventData().observe(this, eventUpdateObserver);
    }

    Observer<Resource<ApiResponseModel>> eventUpdateObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    assert apiResponseModelResource.data != null;
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    progressBar.setVisibility(View.VISIBLE);
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    getEventDetails(eventDetails.getId());
                    break;
            }
        }
    };


    @OnClick(R.id.btnDownloadSelected)
    void downloadSelected() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            checkPermission();
        } else {
            performDownloadAction();
        }
    }

    private void checkPermission() {
        if (UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)) {
            performDownloadAction();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_STORE}, STORE_PERMISSION);
        }
    }

    private void performDownloadAction() {
        for (EventImage item : selectedItems) {
            UtilsFunctions.startDownload(item.getImageUrl(), this);
        }
    }

    @OnClick(R.id.btnDeleteSelected)
    void deleteSelected() {
        if (selectedItems != null) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (UtilsFunctions.isNetworkAvail(EventDetailsActivity.this)) {
                            ArrayList<Integer> eventImageId = new ArrayList<Integer>();
                            for (int i = 0; i < selectedItems.size(); i++) {
                                eventImageId.add(selectedItems.get(i).getImageId());
                            }
                            utilsFunctions.showDialog(this);
                            deleteEventImageWithID(eventImageId);
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };
            UtilsFunctions.showConfirmationDialog(this,
                    getString(R.string.selected_item_delete_confirmation_message), getString(R.string.yes),
                    getString(R.string.no), dialogClickListener);

        } else {
            UtilsFunctions.showToast(EventDetailsActivity.this, AppConstants.shortToast, getString(R.string.select_image));
        }
    }

    @OnClick(R.id.btnDelete)
    void deleteEvent() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (UtilsFunctions.isNetworkAvail(EventDetailsActivity.this)) {
                        doEventDelete(eventDetails.getId());
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        UtilsFunctions.showConfirmationDialog(this,
                getString(R.string.event_delete_confirmation_message), getString(R.string.yes),
                getString(R.string.no), dialogClickListener);

    }

    private void getEventDetails(int id) {
        progressBar.setVisibility(View.VISIBLE);
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.EVENT_ID, id);
        eventViewModel.fetchEventDetails(userAuthPojo.getToken().toString(), map);
        eventViewModel.fetchEventDetailsLiveData().observe(this, fetchEventDetailObserver);
    }

    Observer<Resource<EventDetailsPojo>> fetchEventDetailObserver = new Observer<Resource<EventDetailsPojo>>() {
        @Override
        public void onChanged(Resource<EventDetailsPojo> listResource) {
            switch (listResource.status) {
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    if (listResource.data != null) {
                        eventDetails = listResource.data;
                        toggleClickView.setVisibility(View.VISIBLE);
                        detailsCardView.setVisibility(View.VISIBLE);
                        bottomLayout.setVisibility(View.VISIBLE);
                        spannableString = new SpannableString(eventDetails.getName());
                        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(),
                                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
                        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                        description.setText(listResource.data.getDescription());
                        hostedBy.setText(getResources().getQuantityString(R.plurals.hosted_by,
                                eventDetails.getHostId() == userAuthPojo.getId() ? 1 : 2,
                                eventDetails.getType(),
                                eventDetails.getHostName()));
                        tbPublicity.setChecked(!getString(R.string.private_label)
                                .equals(eventDetails.getType()));
                        gList.clear();
                        gList.addAll(eventDetails.getImages());
                        if (gList.size() == 0) {
                            eventRecyclerView.setVisibility(View.GONE);
                        } else {
                            eventRecyclerView.setVisibility(View.VISIBLE);
                        }
                        mEventImagesAdapter.notifyDataSetChanged();
                        if (eventDetails.getHostId() != userAuthPojo.getId()) {
                            bottomLayout.setVisibility(View.GONE);
                            tbPublicity.setVisibility(View.GONE);
                            mEdit.setVisible(false);
                            description.setEnabled(false);
                            editDescription.setVisibility(View.GONE);
                        } else {
                            mEdit.setVisible(true);
                        }
                        if (!getString(R.string.private_label)
                                .equals(eventDetails.getType())) {
                            btnAddRemoveUser.setVisibility(View.GONE);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
            }
        }
    };

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
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    assert apiResponseModelResource.data != null;
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    progressBar.setVisibility(View.VISIBLE);
                    mEventImagesAdapter.notifyDataSetChanged();
                    getEventDetails(eventDetails.getId());
                    utilsFunctions.hideDialog();
                    break;
            }
        }
    };

    private void doEventDelete(int id) {
        if (UtilsFunctions.isNetworkAvail(EventDetailsActivity.this)) {
            utilsFunctions.showDialog(this);
            deleteEventWithID(id);
            utilsFunctions.hideDialog();
        }
    }

    private void deleteEventWithID(int eventID) {
        eventViewModel.deleteEvent(userAuthPojo.getToken().toString(), eventID);
        eventViewModel.deleteEventData().observe(this, eventDeleteObserver);
    }

    Observer<Resource<ApiResponseModel>> eventDeleteObserver = new Observer<Resource<ApiResponseModel>>() {

        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    assert apiResponseModelResource.data != null;
                    UtilsFunctions.showToast(EventDetailsActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    AppConstants.EVENT_LIST_UPDATE = true;
                    Intent i = new Intent(EventDetailsActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };

    @OnClick(R.id.btnAddRemoveUser)
    void addRemoveUser() {
        if (selectedItems != null)
            selectedItems.clear();
        Intent i = new Intent(EventDetailsActivity.this, EventUserSelectActivity.class);
        i.putExtra(AppConstant.EVENT_ID, eventDetails.getId());
        i.putExtra("USER_ID", userAuthPojo.getId());
        i.putExtra("EVENT_TYPE_ID", eventDetails.getType());
        i.putExtra("EVENT_VICINITY", eventDetails.getNearyby_users());
        i.putExtra("EVENT_REGISTERED", eventDetails.getRegistered_users());
        i.putExtra("EVENT_PHONE_NUMBER", eventDetails.getPhonebook_users());
        i.putExtra("LATITUDE", eventDetails.getLatitude());
        i.putExtra("LONGITUDE", eventDetails.getLongitude());
        someActivityResultLauncher.launch(i);
    }

    @OnClick(R.id.btnAddMedia)
    void addMedia() {
        if (selectedItems != null)
            selectedItems.clear();
        Intent i = new Intent(this, RequestResponseActivity.class);
        i.putExtra(AppConstants.TRANSACTION, AppConstants.EVENT);
        i.putExtra(AppConstant.EVENT_ID, eventDetails.getId());
        someActivityResultLauncher.launch(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && requestCode == STORE_PERMISSION) {
            performDownloadAction();
            LogCapture.e(TAG, "Permission Granted");
        } else {
            LogCapture.e(TAG, "Permission Denied");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mEdit = menu.findItem(R.id.mEdit);
        MenuItem mUpload = menu.findItem(R.id.mUpload);
        mUpload.setVisible(false);
        mEdit.setVisible(false);
        mEdit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (UtilsFunctions.isNetworkAvail(EventDetailsActivity.this)) {
                    showAlertDialogButtonClicked(item.getActionView());
                }

                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
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
    public void onCheckboxClick(List<EventImage> mSelectedItems) {
        selectedItems = mSelectedItems;
        if (selectedItems.size() > 0) {
            selectedActionLayout.setVisibility(View.VISIBLE);
            if (getMyItemsCount() != selectedItems.size()
                    && (eventDetails.getHostId() != userAuthPojo.getId())) {
                btnDeleteSelected.setVisibility(View.GONE);
            } else {
                btnDeleteSelected.setVisibility(View.VISIBLE);
            }
        } else {
            selectedActionLayout.setVisibility(View.GONE);
        }
    }

    private int getMyItemsCount() {
        int myItems = 0;
        for (EventImage item : selectedItems) {
            if (item.getUserId() == (userAuthPojo.getId())) {
                myItems++;
            }
        }
        return myItems;
    }

    @Override
    public void onImageClick(EventImage pojo) {
        openSomeActivityForResult(pojo);
    }

    public void openSomeActivityForResult(EventImage pojo) {
        if (selectedItems != null)
            selectedItems.clear();
        String eventMediaDetail = new Gson().toJson(pojo);
        Intent i = new Intent(EventDetailsActivity.this, GalleryViewActivity.class);
        i.putExtra(AppConstants.SCREEN, AppConstants.EVENT_DETAILS_SCREEN);
        i.putExtra(AppConstants.HIDE_DELETE_BUTTON,
                ((pojo.getUserId() != (userAuthPojo.getId()))
                        && (eventDetails.getHostId() != userAuthPojo.getId())));
        i.putExtra(AppConstants.EVENT_DETAILS, eventMediaDetail);
        someActivityResultLauncher.launch(i);
    }
}
