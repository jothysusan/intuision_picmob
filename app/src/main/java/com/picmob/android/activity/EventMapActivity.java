package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.GPS_REQUEST;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;
import com.picmob.android.R;
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
import com.picmob.android.utils.General;
import com.picmob.android.utils.GpsUtils;
import com.picmob.android.utils.UtilsFunctions;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventMapActivity extends BaseActivity implements com.picmob.android.listeners.BlobStorageService, OnMapReadyCallback {

    @BindView(R.id.btnContinue)
    Button btnContinue;

    private GoogleMap mMap;
    private static final String TAG = "EventMapActivity";
    FusedLocationProviderClient mFusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GpsUtils mGpsUtil;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private LatLng mPosition;
    AutocompleteSupportFragment autocompleteFragment;
    int userId;
    String eventId;
    String eventName;
    String eventDescription;
    String eventImage;
    Bundle extras;
    EventViewModel eventViewModel;
    private UserAuthPojo userAuthPojo;
    UtilsFunctions utilsFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_maps);
        ButterKnife.bind(this);
        extras = getIntent().getExtras();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        SpannableString spannableString = new SpannableString(getString(R.string.choose_event_location));
        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this)
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        utilsFunctions = new UtilsFunctions();
        // Initialize the SDK
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng().toString());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));
                btnContinue.setEnabled(true);
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                btnContinue.setEnabled(false);
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGpsUtil = new GpsUtils(this);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            lastKnownLocation = locationResult.getLastLocation();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude())));
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    };

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getDeviceLocation();
            } else {
                Log.d(TAG, "Location Permission denied. Use defaults.");
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        updateLocationUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                getDeviceLocation();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "GPS denied. Use defaults.");
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                if (isLocationEnabled()) {
                    Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                    locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.
                                lastKnownLocation = task.getResult();
                                Log.e(TAG, "onComplete(): lastKnownLocation -" + lastKnownLocation);
                                if (lastKnownLocation != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(
                                            new LatLng(lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude())));
                                } else {
                                    requestNewLocationData();
                                }
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                } else {
                    mGpsUtil.turnGPSOn(null);
                }
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
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


    @OnClick(R.id.btnContinue)
    void nextPage() {
        if (UtilsFunctions.isNetworkAvail(EventMapActivity.this)) {
            Intent i = new Intent(EventMapActivity.this, CreateEventActivity.class);
            i.putExtra("LATITUDE", mPosition.latitude);
            i.putExtra("LONGITUDE", mPosition.longitude);
            startActivity(i);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void sendEventInvitation() {
        ArrayList<EventUsers> vicinityList = new ArrayList<>();
        ArrayList<EventUsers> registeredList = new ArrayList<>();
        ArrayList<PhonebookUsers> contactList = new ArrayList<>();
        CreateOrUpdateEventPojo createOrUpdateEventPojo =
                new CreateOrUpdateEventPojo(0,userAuthPojo.getId(), eventName,
                        eventDescription, eventImage, eventId, String.valueOf(mPosition.latitude),
                        String.valueOf(mPosition.longitude),
                        vicinityList, registeredList, contactList);
        if (UtilsFunctions.isNetworkAvail(EventMapActivity.this)) {
            utilsFunctions.showDialog(this);
            sendEvent(createOrUpdateEventPojo,false);
            utilsFunctions.hideDialog();
        }
    }

    private void sendEvent(CreateOrUpdateEventPojo createOrUpdateEventPojo,boolean isUpdate) {
        eventViewModel.sendEvent(userAuthPojo.getToken().toString(), createOrUpdateEventPojo, isUpdate);
        eventViewModel.createEventData().observe(this, eventCreateObserver);
    }

    Observer<Resource<ApiResponseModel>> eventCreateObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(EventMapActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(EventMapActivity.this,
                            AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    AppConstants.EVENT_LIST_UPDATE = true;
                    Intent i = new Intent(EventMapActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(19), 1000, null);
        GeocoderHandler handler = new GeocoderHandler();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setPadding(0, 200, 0, 200);

        mMap.setOnCameraIdleListener(() -> {
            mPosition = mMap.getCameraPosition().target;

            double lat = mPosition.latitude;
            double lng = mPosition.longitude;
            Log.e(TAG, "LOCATION: " + mPosition.toString());
            getAddressFromLocation(lat, lng, this, handler);

        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
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
        UtilsFunctions.showToast(EventMapActivity.this, AppConstants.shortToast, error);
        utilsFunctions.hideDialog();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            if (locationAddress != null && !locationAddress.isEmpty()) {
                autocompleteFragment.setText(locationAddress);
                btnContinue.setEnabled(true);
            } else {
                autocompleteFragment.setText(null);
                btnContinue.setEnabled(false);
            }
            Log.e("location Address=", locationAddress);
        }
    }

    public static void getAddressFromLocation(final double latitude, final double longitude, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(0)); //.append("\n");
                        }
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e("Location Address Loader", "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "";
                        bundle.putString("address", result);
                        message.setData(bundle);
                        Log.i(TAG, " Unable to get address for this location.");
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}