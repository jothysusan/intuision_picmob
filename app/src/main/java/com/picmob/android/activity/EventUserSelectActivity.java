package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.CONTACT;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cm.pc.Country;
import com.cm.pc.CountryPicker;
import com.cm.pc.listeners.OnCountryPickerListener;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.picmob.android.R;
import com.picmob.android.adapters.ContactAdapter;
import com.picmob.android.adapters.SelectedRegisteredUsersAdapter;
import com.picmob.android.adapters.UserFromVicinityAdapter;
import com.picmob.android.implementation.BlobStorageService;
import com.picmob.android.listeners.UserFromVicinityListeners;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.events.ContactsPojo;
import com.picmob.android.mvvm.events.CreateOrUpdateEventPojo;
import com.picmob.android.mvvm.events.EventUsers;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.events.PhonebookUsers;
import com.picmob.android.mvvm.events.UserVicinityPojo;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.FileUtils;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventUserSelectActivity extends BaseActivity
        implements com.picmob.android.listeners.BlobStorageService, UserFromVicinityListeners {

    @BindView(R.id.tbSelect)
    Button selectAll;
    @BindView(R.id.vicinity_user_view)
    ShimmerRecyclerViewX vicinityUserView;
    @BindView(R.id.search_name)
    EditText searchName;
    @BindView(R.id.phone_number_text)
    EditText phoneNumberText;
    @BindView(R.id.prediction_list_view)
    ShimmerRecyclerViewX predictionsListView;
    @BindView(R.id.add_user)
    Button addUser;
    @BindView(R.id.add_user_phone)
    Button addUserPhone;
    @BindView(R.id.picmob_user_view_list)
    ShimmerRecyclerViewX registeredUserViewList;
    @BindView(R.id.phone_number_user_view)
    ShimmerRecyclerViewX phoneNumberUserView;
    @BindView(R.id.userVicinityNoData)
    TextView userVicinityNoData;
    @BindView(R.id.registeredUserNoData)
    TextView registeredUserNoData;
    @BindView(R.id.phoneNumberUserNoData)
    TextView phoneNumberNoData;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.user_card)
    CardView cardView;
    @BindView(R.id.button_continue)
    Button continueButton;
    @BindView(R.id.layCC)
    LinearLayout countryCodeLayout;
    @BindView(R.id.tvCC)
    TextView countryCodeTextView;
    Bundle extras;
    int userId;
    int eventId = 0;
    String eventType;
    String eventName;
    String eventDescription;
    String eventImage;
    String eventLatitude;
    String eventLongitude;
    EventViewModel eventViewModel;
    private UserAuthPojo userAuthPojo;
    UserVicinityPojo selectedRegisteredItem;
    UserFromVicinityAdapter userFromVicinityAdapter;
    UserFromVicinityAdapter userRegisteredPredictionsAdapter;
    SelectedRegisteredUsersAdapter selectedRegisteredUsersAdapter;
    ContactAdapter contactAdapter;
    LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();

    ArrayList<EventUsers> preFetchedUserList = new ArrayList<>();
    ArrayList<EventUsers> preFetchedRegisteredUserList = new ArrayList<>();
    List<PhonebookUsers> preFetchedContactList = new ArrayList<>();

    ArrayList<UserVicinityPojo> userFromVicinityList = new ArrayList<>();
    ArrayList<UserVicinityPojo> useRegisteredList = new ArrayList<>();
    ArrayList<UserVicinityPojo> filterList = new ArrayList<>();
    ArrayList<UserVicinityPojo> filteredRegisteredSelectedList = new ArrayList<>();
    List<ContactResult> contactList = new ArrayList<>();
    List<ContactsPojo> phoneContactList = new ArrayList<>();
    ArrayList<UserVicinityPojo> filteredVicinitySelectedList = new ArrayList<>();
    UtilsFunctions utilsFunctions;
    private static final String TAG = "EventUserSelectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_user_select);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        utilsFunctions = new UtilsFunctions();
        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("USER_ID");
            eventId = extras.getInt(AppConstant.EVENT_ID);
            if (eventId != 0) {
                continueButton.setText(getResources().getString(R.string.update_event));
            } else {
                continueButton.setText(getResources().getString(R.string.create_event));
            }
            eventType = extras.getString("EVENT_TYPE_ID");
            eventName = extras.getString("EVENT_NAME");
            eventDescription = extras.getString("EVENT_DESCRIPTION");
            eventImage = extras.getString("EVENT_IMAGE");
            eventLongitude = extras.getString("LONGITUDE");
            eventLatitude = extras.getString("LATITUDE");
            preFetchedUserList = (ArrayList<EventUsers>) getIntent()
                    .getSerializableExtra("EVENT_VICINITY");
            preFetchedRegisteredUserList = (ArrayList<EventUsers>) getIntent()
                    .getSerializableExtra("EVENT_REGISTERED");
            preFetchedContactList = (ArrayList<PhonebookUsers>) getIntent()
                    .getSerializableExtra("EVENT_PHONE_NUMBER");
        }
        vicinityUserView.showShimmerAdapter();
        vicinityUserView.setHasFixedSize(true);
        vicinityUserView.setDrawingCacheEnabled(true);
        vicinityUserView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        userFromVicinityAdapter = new UserFromVicinityAdapter(getApplicationContext(),
                EventUserSelectActivity.this, userFromVicinityList, false);
        vicinityUserView.setAdapter(userFromVicinityAdapter);

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectAll.getText().equals(getResources().getString(R.string.select_all))) {
                    for (int i = 0; i < userFromVicinityList.size(); i++) {
                        userFromVicinityAdapter.changeCheckboxStatus(userFromVicinityList.get(i), true);
                    }
                    selectAll.setText(getResources().getString(R.string.tv_unselect_all_btn_text));
                } else {
                    for (int i = 0; i < userFromVicinityList.size(); i++) {
                        userFromVicinityAdapter.changeCheckboxStatus(userFromVicinityList.get(i), false);
                    }
                    selectAll.setText(getResources().getString(R.string.select_all));
                }
            }
        });

        predictionsListView.setHasFixedSize(true);
        predictionsListView.setDrawingCacheEnabled(true);
        predictionsListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        userRegisteredPredictionsAdapter = new UserFromVicinityAdapter(getApplicationContext(),
                EventUserSelectActivity.this, filterList, true);
        predictionsListView.setAdapter(userRegisteredPredictionsAdapter);

        registeredUserViewList.setHasFixedSize(true);
        registeredUserViewList.setDrawingCacheEnabled(true);
        registeredUserViewList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        selectedRegisteredUsersAdapter = new SelectedRegisteredUsersAdapter(filteredRegisteredSelectedList,
                getApplicationContext(),
                EventUserSelectActivity.this);
        registeredUserViewList.setAdapter(selectedRegisteredUsersAdapter);


        searchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                predictionsListView.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                predictionsListView.setVisibility(View.VISIBLE);
                predictionsListView.showShimmerAdapter();
                filter(s.toString());
            }
        });

        phoneNumberUserView.setHasFixedSize(true);
        phoneNumberUserView.setDrawingCacheEnabled(true);
        phoneNumberUserView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        contactAdapter = new ContactAdapter(phoneContactList,
                getApplicationContext(),
                EventUserSelectActivity.this);
        phoneNumberUserView.setAdapter(contactAdapter);
        phoneNumberUserView.hideShimmerAdapter();

        if (preFetchedContactList != null) {
            for (int i = 0; i < preFetchedContactList.size(); i++) {
                phoneContactList.add(new ContactsPojo("", preFetchedContactList.get(i).getPhone_number()));
            }
        }


        contactAdapter.notifyDataSetChanged();

        phoneNumberText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!v.getText().toString().isEmpty()) {
                        String contactNumber = countryCodeTextView.getText().toString() + " " + v.getText().toString();
                        phoneContactList.add(new ContactsPojo("", contactNumber));
                        contactAdapter.notifyDataSetChanged();
                        phoneNumberText.setText("");
                        countryCodeTextView.setText("+1");
                        return true;
                    }
                }
                return false;
            }
        });

        map.put(AppConstant.ID, userId);
        map.put(AppConstant.LATITUDE, eventLatitude);
        map.put(AppConstant.LONGITUDE, eventLongitude);
        map.put(AppConstant.RADIUS, AppConstants.DEFAULT_USER_VICINITY_RADIUS);
        getUsers();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        SpannableString spannableString = new SpannableString(getString(R.string.select_users_label));
        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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

    @OnClick(R.id.layCC)
    void showCC() {
        CountryPicker countryPicker =
                new CountryPicker.Builder().with(this)
                        .listener(new OnCountryPickerListener() {
                            @Override
                            public void onSelectCountry(Country country) {
                                countryCodeTextView.setText(country.getDialCode());
                            }
                        }).style(R.style.Theme_Design_BottomSheetDialog)
                        .canSearch(true)
                        .sortBy(CountryPicker.SORT_BY_NAME)
                        .theme(CountryPicker.THEME_NEW).build();

        countryPicker.showBottomSheet(this);
    }

    private void filter(String text) {
        if (text.isEmpty()) {
            filterList.clear();
        } else {
            filterList.clear();
            for (UserVicinityPojo item : useRegisteredList) {
                if (item.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filterList.add(item);
                }
            }
        }
        userRegisteredPredictionsAdapter.filterList(filterList);
        predictionsListView.hideShimmerAdapter();
    }

    private void getUsers() {
        eventViewModel.getUsersList(userAuthPojo.getToken().toString(), map);
        eventViewModel.getUserVicinityLiveData().observe(this, userVicinityObserver);
        eventViewModel.getRegisteredUserLiveData().observe(this, registeredUserObserver);
    }

    Observer<Resource<List<UserVicinityPojo>>> userVicinityObserver = new Observer<Resource<List<UserVicinityPojo>>>() {
        @Override
        public void onChanged(Resource<List<UserVicinityPojo>> listResource) {
            switch (listResource.status) {
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    vicinityUserView.hideShimmerAdapter();
                    vicinityUserView.setVisibility(View.GONE);
                    userVicinityNoData.setVisibility(View.VISIBLE);
                    UtilsFunctions.showToast(EventUserSelectActivity.this,
                            AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    cardView.setVisibility(View.VISIBLE);
                    continueButton.setVisibility(View.VISIBLE);
                    vicinityUserView.showShimmerAdapter();
                    userFromVicinityList.clear();
                    userFromVicinityList.addAll(listResource.data);
                    if (userFromVicinityList.size() == 0) {
                        vicinityUserView.setVisibility(View.GONE);
                        userVicinityNoData.setVisibility(View.VISIBLE);
                    }
                    if (preFetchedUserList != null) {
                        for (int i = 0; i < preFetchedUserList.size(); i++) {
                            for (int j = 0; j < userFromVicinityList.size(); j++) {
                                if (userFromVicinityList.get(j).getId() == preFetchedUserList.get(i).getUserid())
                                    userFromVicinityList.get(j).setSelected(true);
                            }
                        }
                    }
                    vicinityUserView.hideShimmerAdapter();
                    userFromVicinityAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    Observer<Resource<List<UserVicinityPojo>>> registeredUserObserver = new Observer<Resource<List<UserVicinityPojo>>>() {
        @Override
        public void onChanged(Resource<List<UserVicinityPojo>> listResource) {
            switch (listResource.status) {
                case ERROR:
                    predictionsListView.hideShimmerAdapter();
                    UtilsFunctions.showToast(EventUserSelectActivity.this, AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    if (listResource.data != null) {
                        useRegisteredList.clear();
                        useRegisteredList.addAll(listResource.data);
                    }
                    if (useRegisteredList.size() == 0) {
                        registeredUserNoData.setVisibility(View.VISIBLE);
                    }

                    if (preFetchedRegisteredUserList != null) {
                        filterList.clear();
                        for (int i = 0; i < preFetchedRegisteredUserList.size(); i++) {
                            for (int j = 0; j < useRegisteredList.size(); j++) {
                                if (useRegisteredList.get(j).getId()
                                        == preFetchedRegisteredUserList.get(i).getUserid()) {
                                    filteredRegisteredSelectedList.add(useRegisteredList.get(j));
                                }
                            }
                        }
                    }
                    selectedRegisteredUsersAdapter.notifyDataSetChanged();

                    predictionsListView.hideShimmerAdapter();
                    userRegisteredPredictionsAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @OnClick(R.id.button_continue)
    void continueButton() {
        filteredVicinitySelectedList.clear();
        for (int i = 0; i < userFromVicinityList.size(); i++) {
            if (userFromVicinityList.get(i).isSelected()) {
                filteredVicinitySelectedList.add(userFromVicinityList.get(i));
            }
        }
        if (filteredVicinitySelectedList.size() > 0 | filteredRegisteredSelectedList.size() > 0
                | phoneContactList.size() > 0) {
            try {
                utilsFunctions.showDialog(EventUserSelectActivity.this);
                if (eventImage != null) {
                    if (AppConstants.mPhotoPath != null) {
                        new BlobStorageService(this).uploadFile(new File(eventImage));
                    } else {
                        Uri eventImageURI = Uri.parse(eventImage);
                        new BlobStorageService(this)
                                .uploadFile(FileUtils.getFileFromUri(this, eventImageURI));
                    }
                } else {
                    sendEventInvitation();
                }
                utilsFunctions.hideDialog();
            } catch (Exception e) {
                utilsFunctions.hideDialog();
                e.printStackTrace();
            }
        } else {
            UtilsFunctions.showToast(EventUserSelectActivity.this,
                    AppConstants.shortToast, getString(R.string.no_users_selected));
        }
    }

    private void sendEventInvitation() {
        ArrayList<EventUsers> vicinityList = new ArrayList<>();
        ArrayList<EventUsers> registeredList = new ArrayList<>();
        ArrayList<PhonebookUsers> contactList = new ArrayList<>();


        for (int i = 0; i < filteredVicinitySelectedList.size(); i++) {
            vicinityList.add(new EventUsers(filteredVicinitySelectedList.get(i).getId()));
        }
        for (int i = 0; i < filteredRegisteredSelectedList.size(); i++) {
            registeredList.add(new EventUsers(filteredRegisteredSelectedList.get(i).getId()));
        }

        for (int i = 0; i < phoneContactList.size(); i++) {
            contactList.add(new PhonebookUsers(phoneContactList.get(i).getPhoneNumber()));
        }


        CreateOrUpdateEventPojo createOrUpdateEventPojo =
                new CreateOrUpdateEventPojo(eventId, userAuthPojo.getId(), eventName,
                        eventDescription, eventImage, eventType, eventLatitude,
                        eventLongitude, vicinityList, registeredList, contactList);
        if (UtilsFunctions.isNetworkAvail(EventUserSelectActivity.this)) {
            utilsFunctions.showDialog(this);
            sendEvent(createOrUpdateEventPojo, eventId != 0);
        }
    }

    private void sendEvent(CreateOrUpdateEventPojo createOrUpdateEventPojo, boolean isUpdate) {
        eventViewModel.sendEvent(userAuthPojo.getToken().toString(), createOrUpdateEventPojo, isUpdate);
        if (isUpdate)
            eventViewModel.createEventData().observe(this, eventUpdateObserver);
        else
            eventViewModel.createEventData().observe(this, eventCreateObserver);
    }

    Observer<Resource<ApiResponseModel>> eventUpdateObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(EventUserSelectActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    setResult(Activity.RESULT_OK);
                    AppConstants.EVENT_LIST_UPDATE = true;
                    UtilsFunctions.showToast(EventUserSelectActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    finish();
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> eventCreateObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(EventUserSelectActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    if (eventId != 0) {
                        setResult(Activity.RESULT_OK);
                    } else {
                        UtilsFunctions.showToast(EventUserSelectActivity.this,
                                AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                        AppConstants.EVENT_LIST_UPDATE = true;
                        Intent i = new Intent(EventUserSelectActivity.this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                    finish();
                    break;
            }
        }
    };

    @OnClick(R.id.add_user_phone)
    void addFromPhoneBook() {
        checkPermission();
    }

    private void checkPermission() {
        if (UtilsFunctions.hasMultiplePermissions(this, CONTACT)) {
            contactIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CONTACT}, STORE_PERMISSION);
        }
    }


    private void contactIntent() {
        new MultiContactPicker.Builder(this) //Activity/fragment context
                .hideScrollbar(true) //Optional - default: false
                .showTrack(false) //Optional - default: true
                .searchIconColor(Color.WHITE) //Option - default: White
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(ContextCompat.getColor(this, R.color.purple_500)) //Optional - default: Azure Blue
                .bubbleColor(ContextCompat.getColor(this, R.color.purple_300)) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .setTitleText("Select Contacts") //Optional - default: Select Contacts
                .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                .limitToColumn(LimitColumn.PHONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .showPickerForResult(AppConstants.CONTACT_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            checkPermission();
            LogCapture.e(TAG, "Permission Granted, Now you can use local drive .CONFIRMED");
        } else {
            LogCapture.e(TAG, "Permission Denied, You cannot use local drive .");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<ContactResult> results = new ArrayList<>(MultiContactPicker.obtainResult(data));
                List<LinkedTreeMap<String, Object>> list = new ArrayList<>();

                for (ContactResult contactResult : results) {
                    LinkedTreeMap<String, Object> linkedTreeMap = new LinkedTreeMap<>();
                    linkedTreeMap.put(AppConstant.NAME, contactResult.getDisplayName());
                    linkedTreeMap.put(AppConstant.PHONE, contactResult.getPhoneNumbers().get(0).getNumber());
                    linkedTreeMap.put(AppConstant.IDENTIFIER, contactResult.getContactID());
                    list.add(linkedTreeMap);
                    contactList.add(contactResult);
                    phoneContactList.add(new ContactsPojo(contactResult.getDisplayName(),
                            contactResult.getPhoneNumbers().get(0).getNumber()));
                    Log.v(TAG, "onActivityResult: " + contactResult.getDisplayName()
                            + "===" + contactResult.getPhoneNumbers().get(0).getNumber());
                }
                contactAdapter.notifyDataSetChanged();

            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        }
    }

    @Override
    protected void onStart() {
        closeKeyboard();
        super.onStart();
    }

    @Override
    protected void onResume() {
        closeKeyboard();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isFinishing())
            utilsFunctions.hideDialog();
        closeKeyboard();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isFinishing())
            utilsFunctions.hideDialog();
        super.onStop();
    }

    @OnClick(R.id.add_user)
    void addUserButton() {
        registeredUserViewList.showShimmerAdapter();
        if (filterList.size() != 0) {
            if (!filteredRegisteredSelectedList.contains(selectedRegisteredItem)) {
                filteredRegisteredSelectedList.add(selectedRegisteredItem);
            }
        }
        selectedRegisteredUsersAdapter.notifyDataSetChanged();
        searchName.setText("");
        registeredUserViewList.hideShimmerAdapter();
    }


    @Override
    public void onItemSelected(UserVicinityPojo userVicinityPojo) {
        selectedRegisteredItem = userVicinityPojo;
    }

    private void closeKeyboard() {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();
        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {
            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void getUrl(URI url) {
        eventImage = url.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendEventInvitation();
            }
        });
    }


    @Override
    public void error(String error) {
        UtilsFunctions.showToast(EventUserSelectActivity.this, AppConstants.shortToast, error);
        utilsFunctions.hideDialog();
    }
}